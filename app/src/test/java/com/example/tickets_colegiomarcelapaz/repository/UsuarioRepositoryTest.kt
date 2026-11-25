package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.data.Usuario
import com.example.tickets_colegiomarcelapaz.data.UsuarioDao
import com.example.tickets_colegiomarcelapaz.remote.LoginRequest
import com.example.tickets_colegiomarcelapaz.remote.RegistroRequest
import com.example.tickets_colegiomarcelapaz.remote.UsuarioApiService
import com.example.tickets_colegiomarcelapaz.remote.UsuarioDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class UsuarioRepositoryTest {

    private val dao = mockk<UsuarioDao>(relaxUnitFun = true)
    private val api = mockk<UsuarioApiService>()
    private val repository = UsuarioRepository(dao, api)

    private val dto = UsuarioDto(id = 7, nombre = "Mazi", email = "mazi@colegio.cl")
    private val usuario = Usuario(id = 7, nombre = "Mazi", email = "mazi@colegio.cl")

    private fun httpError(codigo: Int) = HttpException(Response.error<Unit>(codigo, "".toResponseBody()))

    @Test
    fun `login normaliza el correo y guarda el usuario en Room`() = runTest {
        coEvery { api.login(any()) } returns dto

        val resultado = repository.login("  MAZI@Colegio.cl ", "clave1234")

        assertEquals(usuario, resultado)
        coVerify { api.login(LoginRequest("mazi@colegio.cl", "clave1234")) }
        coVerify { dao.guardar(usuario) }
    }

    @Test
    fun `registrar envia los datos limpios y guarda el usuario en Room`() = runTest {
        coEvery { api.registrar(any()) } returns dto

        val resultado = repository.registrar(" Mazi ", "Mazi@colegio.cl", "clave1234")

        assertEquals(usuario, resultado)
        coVerify { api.registrar(RegistroRequest("Mazi", "mazi@colegio.cl", "clave1234")) }
        coVerify { dao.guardar(usuario) }
    }

    @Test
    fun `si el login es rechazado no se guarda nada en Room`() = runTest {
        coEvery { api.login(any()) } throws httpError(401)

        val resultado = runCatching { repository.login("mazi@colegio.cl", "mala1234") }

        assertTrue(resultado.exceptionOrNull() is HttpException)
        coVerify(exactly = 0) { dao.guardar(any()) }
    }

    @Test
    fun `obtenerPorId consulta el servidor y actualiza Room`() = runTest {
        coEvery { api.buscar(7) } returns dto

        val resultado = repository.obtenerPorId(7)

        assertEquals(usuario, resultado)
        coVerify { dao.guardar(usuario) }
    }

    @Test
    fun `obtenerPorId sin conexion usa el usuario guardado en Room`() = runTest {
        coEvery { api.buscar(7) } throws IOException("sin red")
        coEvery { dao.buscarPorId(7) } returns usuario

        assertEquals(usuario, repository.obtenerPorId(7))
    }

    @Test
    fun `obtenerPorId devuelve null si el usuario ya no existe en el servidor`() = runTest {
        coEvery { api.buscar(7) } throws httpError(404)

        assertNull(repository.obtenerPorId(7))
    }

    @Test
    fun `obtenerPorId con error del servidor usa Room`() = runTest {
        coEvery { api.buscar(7) } throws httpError(500)
        coEvery { dao.buscarPorId(7) } returns usuario

        assertEquals(usuario, repository.obtenerPorId(7))
    }
}
