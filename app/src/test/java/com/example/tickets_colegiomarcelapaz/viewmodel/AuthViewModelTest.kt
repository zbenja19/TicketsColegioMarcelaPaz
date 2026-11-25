package com.example.tickets_colegiomarcelapaz.viewmodel

import com.example.tickets_colegiomarcelapaz.MainDispatcherRule
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Usuario
import com.example.tickets_colegiomarcelapaz.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<UsuarioRepository>()
    private val session = mockk<SessionManager>(relaxUnitFun = true)

    private val usuario = Usuario(id = 7, nombre = "Mazi", email = "mazi@colegio.cl")

    private fun httpError(codigo: Int) = HttpException(Response.error<Unit>(codigo, "".toResponseBody()))

    private fun crearViewModel(idGuardado: Long? = null): AuthViewModel {
        every { session.usuarioId } returns flowOf(idGuardado)
        return AuthViewModel(repository, session)
    }

    private fun AuthViewModel.llenarLogin() {
        onLoginEmail("mazi@colegio.cl")
        onLoginPassword("clave1234")
    }

    private fun AuthViewModel.llenarRegistro() {
        onRegisterNombre("Mazi")
        onRegisterEmail("mazi@colegio.cl")
        onRegisterPassword("clave1234")
        onRegisterConfirmar("clave1234")
    }

    @Test
    fun `sin sesion guardada la verificacion termina sin usuario`() = runTest {
        val viewModel = crearViewModel()

        assertTrue(viewModel.state.value.sesionVerificada)
        assertNull(viewModel.state.value.usuario)
    }

    @Test
    fun `restaura la sesion guardada consultando el repositorio`() = runTest {
        coEvery { repository.obtenerPorId(7) } returns usuario

        val viewModel = crearViewModel(idGuardado = 7)

        assertEquals(usuario, viewModel.state.value.usuario)
    }

    @Test
    fun `si el usuario guardado ya no existe se cierra la sesion`() = runTest {
        coEvery { repository.obtenerPorId(7) } returns null

        crearViewModel(idGuardado = 7)

        coVerify { session.cerrar() }
    }

    @Test
    fun `login correcto guarda la sesion y navega`() = runTest {
        coEvery { repository.login(any(), any()) } returns usuario
        val viewModel = crearViewModel()
        viewModel.llenarLogin()
        var exito = false

        viewModel.login { exito = true }

        assertTrue(exito)
        assertEquals(usuario, viewModel.state.value.usuario)
        coVerify { session.guardar(7) }
    }

    @Test
    fun `login con credenciales incorrectas muestra el error 401`() = runTest {
        coEvery { repository.login(any(), any()) } throws httpError(401)
        val viewModel = crearViewModel()
        viewModel.llenarLogin()
        var exito = false

        viewModel.login { exito = true }

        assertFalse(exito)
        assertFalse(viewModel.state.value.cargando)
        assertEquals("Correo o contraseña incorrectos", viewModel.state.value.error)
        coVerify(exactly = 0) { session.guardar(any()) }
    }

    @Test
    fun `login sin conexion muestra el mensaje de servidor apagado`() = runTest {
        coEvery { repository.login(any(), any()) } throws IOException("sin red")
        val viewModel = crearViewModel()
        viewModel.llenarLogin()

        viewModel.login {}

        assertEquals(AuthViewModel.SIN_CONEXION, viewModel.state.value.error)
    }

    @Test
    fun `login con formulario vacio no llama al repositorio`() = runTest {
        val viewModel = crearViewModel()

        viewModel.login {}

        assertTrue(viewModel.loginForm.value.emailError != null)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `registro correcto guarda la sesion`() = runTest {
        coEvery { repository.registrar(any(), any(), any()) } returns usuario
        val viewModel = crearViewModel()
        viewModel.llenarRegistro()
        var exito = false

        viewModel.registrar { exito = true }

        assertTrue(exito)
        coVerify { session.guardar(7) }
    }

    @Test
    fun `registro con correo repetido muestra el error 409`() = runTest {
        coEvery { repository.registrar(any(), any(), any()) } throws httpError(409)
        val viewModel = crearViewModel()
        viewModel.llenarRegistro()

        viewModel.registrar {}

        assertEquals("Este correo ya está registrado", viewModel.state.value.error)
    }

    @Test
    fun `registro rechazado por validacion muestra el error 400`() = runTest {
        coEvery { repository.registrar(any(), any(), any()) } throws httpError(400)
        val viewModel = crearViewModel()
        viewModel.llenarRegistro()

        viewModel.registrar {}

        assertEquals("Los datos no son válidos", viewModel.state.value.error)
    }

    @Test
    fun `cerrar sesion borra el usuario`() = runTest {
        coEvery { repository.obtenerPorId(7) } returns usuario
        val viewModel = crearViewModel(idGuardado = 7)
        var listo = false

        viewModel.cerrarSesion { listo = true }

        assertTrue(listo)
        assertNull(viewModel.state.value.usuario)
        coVerify { session.cerrar() }
    }
}
