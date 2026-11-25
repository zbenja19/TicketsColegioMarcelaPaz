package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.data.TicketDao
import com.example.tickets_colegiomarcelapaz.remote.TicketApiService
import com.example.tickets_colegiomarcelapaz.remote.TicketDto
import com.example.tickets_colegiomarcelapaz.remote.TicketRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TicketRepositoryTest {

    private val dao = mockk<TicketDao>(relaxUnitFun = true)
    private val api = mockk<TicketApiService>()
    private val repository = TicketRepository(dao, api)

    private fun dto(id: Long = 5, estado: String = "Abierto") = TicketDto(
        id = id,
        usuarioId = 1,
        titulo = "Luz rota sala 12",
        descripcion = "El tubo parpadea todo el dia",
        sector = "Sala 12",
        urgencia = "Alta",
        estado = estado,
        fecha = 1000L,
    )

    private fun ticket(id: Long = 5, estado: String = "Abierto", foto: String? = null) = Ticket(
        id = id,
        usuarioId = 1,
        titulo = "Luz rota sala 12",
        descripcion = "El tubo parpadea todo el dia",
        sector = "Sala 12",
        urgencia = "Alta",
        estado = estado,
        fecha = 1000L,
        fotoRuta = foto,
    )

    @Test
    fun `tickets devuelve el flujo de Room del usuario`() {
        val flujo = flowOf(listOf(ticket()))
        every { dao.listarPorUsuario(1) } returns flujo

        val resultado = repository.tickets(1)

        assertSame(flujo, resultado)
    }

    @Test
    fun `crear envia el ticket a la API y lo guarda en Room con el id del servidor`() = runTest {
        coEvery { api.crear(any()) } returns dto(id = 5)

        val id = repository.crear(ticket(id = 0, foto = "/fotos/luz.jpg"))

        assertEquals(5L, id)
        coVerify {
            api.crear(
                TicketRequest(
                    usuarioId = 1,
                    titulo = "Luz rota sala 12",
                    descripcion = "El tubo parpadea todo el dia",
                    sector = "Sala 12",
                    urgencia = "Alta",
                )
            )
        }
        coVerify { dao.guardarTodos(listOf(ticket(id = 5, foto = "/fotos/luz.jpg"))) }
    }

    @Test
    fun `si la API falla al crear no se guarda nada en Room`() = runTest {
        coEvery { api.crear(any()) } throws IOException("sin red")

        val resultado = runCatching { repository.crear(ticket(id = 0)) }

        assertTrue(resultado.exceptionOrNull() is IOException)
        coVerify(exactly = 0) { dao.guardarTodos(any()) }
    }

    @Test
    fun `avanzarEstado guarda en Room el estado que responde el servidor`() = runTest {
        coEvery { api.avanzarEstado(5) } returns dto(estado = "En Proceso")

        repository.avanzarEstado(ticket(foto = "/fotos/luz.jpg"))

        coVerify { dao.actualizar(ticket(estado = "En Proceso", foto = "/fotos/luz.jpg")) }
    }

    @Test
    fun `sincronizar guarda los tickets remotos conservando las fotos locales`() = runTest {
        coEvery { api.listar(1) } returns listOf(dto(id = 5), dto(id = 6))
        every { dao.listarPorUsuario(1) } returns flowOf(listOf(ticket(id = 5, foto = "/fotos/luz.jpg")))

        repository.sincronizar(1)

        coVerify { dao.guardarTodos(listOf(ticket(id = 5, foto = "/fotos/luz.jpg"), ticket(id = 6))) }
        coVerify { dao.borrarAusentes(1, listOf(5L, 6L)) }
    }

    @Test
    fun `eliminar borra en Room cuando el servidor responde OK`() = runTest {
        coEvery { api.eliminar(5) } returns Response.success(Unit)

        repository.eliminar(ticket())

        coVerify { dao.eliminar(ticket()) }
    }

    @Test
    fun `eliminar borra en Room si el ticket ya no existia en el servidor`() = runTest {
        coEvery { api.eliminar(5) } returns Response.error(404, "".toResponseBody())

        repository.eliminar(ticket())

        coVerify { dao.eliminar(ticket()) }
    }

    @Test
    fun `eliminar no borra en Room si el servidor falla`() = runTest {
        coEvery { api.eliminar(5) } returns Response.error(500, "".toResponseBody())

        val resultado = runCatching { repository.eliminar(ticket()) }

        assertTrue(resultado.exceptionOrNull() is HttpException)
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }
}