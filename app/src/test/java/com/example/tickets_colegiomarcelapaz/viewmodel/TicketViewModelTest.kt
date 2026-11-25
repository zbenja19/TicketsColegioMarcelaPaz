package com.example.tickets_colegiomarcelapaz.viewmodel

import com.example.tickets_colegiomarcelapaz.MainDispatcherRule
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TicketRepository>(relaxUnitFun = true)
    private val session = mockk<SessionManager>()

    private val alta = ticket(1, "Alta")
    private val baja = ticket(2, "Baja")
    private val media = ticket(3, "Media")

    private fun ticket(id: Long, urgencia: String) = Ticket(
        id = id,
        usuarioId = 1,
        titulo = "Ticket $id",
        descripcion = "Descripcion de prueba",
        sector = "Patio",
        urgencia = urgencia,
        fecha = id,
    )

    @Before
    fun setUp() {
        every { session.usuarioId } returns flowOf(1L)
        every { repository.tickets(1) } returns flowOf(listOf(baja, alta, media))
    }

    @Test
    fun `al crearse sincroniza los tickets del usuario conectado`() = runTest {
        TicketViewModel(repository, session)

        coVerify { repository.sincronizar(1) }
    }

    @Test
    fun `filtro Todas muestra la lista completa`() = runTest {
        val viewModel = TicketViewModel(repository, session)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.tickets.collect {} }

        assertEquals(listOf(baja, alta, media), viewModel.tickets.value)
    }

    @Test
    fun `filtro Alta muestra solo los tickets de urgencia alta`() = runTest {
        val viewModel = TicketViewModel(repository, session)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.tickets.collect {} }

        viewModel.setFiltroUrgencia("Alta")

        assertEquals(listOf(alta), viewModel.tickets.value)
    }

    @Test
    fun `filtro Alta a Baja ordena por prioridad`() = runTest {
        val viewModel = TicketViewModel(repository, session)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.tickets.collect {} }

        viewModel.setFiltroUrgencia("Alta a Baja")

        assertEquals(listOf(alta, media, baja), viewModel.tickets.value)
    }

    @Test
    fun `sin conexion muestra el mensaje de error y deja de cargar`() = runTest {
        coEvery { repository.sincronizar(1) } throws IOException("sin red")

        val viewModel = TicketViewModel(repository, session)

        assertEquals("Sin conexión con el servidor. Revisa que esté encendido.", viewModel.error.value)
        assertFalse(viewModel.cargando.value)
    }

    @Test
    fun `error HTTP al avanzar estado muestra el codigo`() = runTest {
        coEvery { repository.avanzarEstado(any()) } throws HttpException(Response.error<Unit>(500, "".toResponseBody()))
        val viewModel = TicketViewModel(repository, session)

        viewModel.avanzarEstado(alta)

        assertEquals("Error del servidor (500).", viewModel.error.value)
    }

    @Test
    fun `eliminar delega en el repositorio`() = runTest {
        val viewModel = TicketViewModel(repository, session)

        viewModel.eliminar(baja)

        coVerify { repository.eliminar(baja) }
    }

    @Test
    fun `limpiarError borra el mensaje`() = runTest {
        coEvery { repository.sincronizar(1) } throws IOException("sin red")
        val viewModel = TicketViewModel(repository, session)

        viewModel.limpiarError()

        assertNull(viewModel.error.value)
    }
}