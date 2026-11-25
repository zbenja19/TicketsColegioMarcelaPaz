package com.example.tickets_colegiomarcelapaz.viewmodel

import com.example.tickets_colegiomarcelapaz.MainDispatcherRule
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NuevoTicketViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TicketRepository>()
    private val session = mockk<SessionManager>()
    private lateinit var viewModel: NuevoTicketViewModel

    @Before
    fun setUp() {
        every { session.usuarioId } returns flowOf(1L)
        viewModel = NuevoTicketViewModel(repository, session)
    }

    private fun llenarFormularioValido() {
        viewModel.onTitulo("  Luz rota sala 12  ")
        viewModel.onDescripcion("El tubo fluorescente parpadea")
        viewModel.onSector("Patio")
        viewModel.onUrgencia("Alta")
    }

    @Test
    fun `formulario vacio muestra errores y no llama al repositorio`() = runTest {
        var exito = false

        viewModel.guardar { exito = true }

        val form = viewModel.form.value
        assertNotNull(form.tituloError)
        assertNotNull(form.descripcionError)
        assertNotNull(form.urgenciaError)
        assertFalse(exito)
        coVerify(exactly = 0) { repository.crear(any()) }
    }

    @Test
    fun `formulario valido crea el ticket con los datos limpios y navega`() = runTest {
        val creado = slot<Ticket>()
        coEvery { repository.crear(capture(creado)) } returns 5L
        llenarFormularioValido()
        var exito = false

        viewModel.guardar { exito = true }

        assertTrue(exito)
        assertEquals(1L, creado.captured.usuarioId)
        assertEquals("Luz rota sala 12", creado.captured.titulo)
        assertEquals("Patio", creado.captured.sector)
        assertEquals("Alta", creado.captured.urgencia)
    }

    @Test
    fun `sector Otro usa el lugar escrito por el usuario`() = runTest {
        val creado = slot<Ticket>()
        coEvery { repository.crear(capture(creado)) } returns 5L
        llenarFormularioValido()
        viewModel.onSector("Otro")
        viewModel.onLugarOtro("Casino")

        viewModel.guardar {}

        assertEquals("Casino", creado.captured.sector)
    }

    @Test
    fun `cambiar de Otro a otro sector limpia el lugar escrito`() {
        viewModel.onSector("Otro")
        viewModel.onLugarOtro("Casino")

        viewModel.onSector("Patio")

        assertEquals("", viewModel.form.value.lugarOtro)
    }

    @Test
    fun `sin conexion muestra error y no navega`() = runTest {
        coEvery { repository.crear(any()) } throws IOException("sin red")
        llenarFormularioValido()
        var exito = false

        viewModel.guardar { exito = true }

        assertFalse(exito)
        assertFalse(viewModel.form.value.guardando)
        assertEquals("Sin conexión con el servidor. Revisa que esté encendido.", viewModel.form.value.errorServidor)
    }

    @Test
    fun `rechazo del servidor muestra el codigo HTTP`() = runTest {
        coEvery { repository.crear(any()) } throws HttpException(Response.error<Unit>(400, "".toResponseBody()))
        llenarFormularioValido()

        viewModel.guardar {}

        assertEquals("El servidor rechazó el reporte (error 400).", viewModel.form.value.errorServidor)
    }
}