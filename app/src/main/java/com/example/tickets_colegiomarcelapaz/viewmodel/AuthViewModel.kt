package com.example.tickets_colegiomarcelapaz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tickets_colegiomarcelapaz.TicketsApp
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Usuario
import com.example.tickets_colegiomarcelapaz.repository.UsuarioRepository
import com.example.tickets_colegiomarcelapaz.validation.validateConfirmPassword
import com.example.tickets_colegiomarcelapaz.validation.validateEmail
import com.example.tickets_colegiomarcelapaz.validation.validateName
import com.example.tickets_colegiomarcelapaz.validation.validatePassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val usuario: Usuario? = null,
    /** false mientras se lee la sesión guardada al abrir la app. */
    val sesionVerificada: Boolean = false,
)

/**
 * Estado del formulario de login. Los errores se calculan a partir de los campos;
 * [intentado] hace que solo se muestren después del primer "Iniciar sesión".
 */
data class LoginForm(
    val email: String = "",
    val password: String = "",
    val intentado: Boolean = false,
) {
    val emailError get() = if (intentado) validateEmail(email) else null
    val passwordError get() = if (intentado) validatePassword(password, strict = false) else null
    val esValido get() = validateEmail(email) == null && validatePassword(password, strict = false) == null
}

data class RegisterForm(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmar: String = "",
    val intentado: Boolean = false,
) {
    val nombreError get() = if (intentado) validateName(nombre) else null
    val emailError get() = if (intentado) validateEmail(email) else null
    val passwordError get() = if (intentado) validatePassword(password, strict = true) else null
    val confirmarError get() = if (intentado) validateConfirmPassword(password, confirmar) else null
    val esValido
        get() = validateName(nombre) == null &&
                validateEmail(email) == null &&
                validatePassword(password, strict = true) == null &&
                validateConfirmPassword(password, confirmar) == null
}

class AuthViewModel(
    private val repository: UsuarioRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginForm())
    val loginForm: StateFlow<LoginForm> = _loginForm.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterForm())
    val registerForm: StateFlow<RegisterForm> = _registerForm.asStateFlow()

    init {
        restaurarSesion()
    }

    private fun restaurarSesion() {
        viewModelScope.launch {
            val id = session.usuarioId.first()
            val usuario = id?.let { repository.obtenerPorId(it) }
            if (id != null && usuario == null) session.cerrar() // sesión huérfana
            _state.update { it.copy(usuario = usuario, sesionVerificada = true) }
        }
    }

    // ---------- Cambios de campos (los llama la UI en cada tecla) ----------

    fun onLoginEmail(v: String) = _loginForm.update { it.copy(email = v) }
    fun onLoginPassword(v: String) = _loginForm.update { it.copy(password = v) }

    fun onRegisterNombre(v: String) = _registerForm.update { it.copy(nombre = v) }
    fun onRegisterEmail(v: String) = _registerForm.update { it.copy(email = v) }
    fun onRegisterPassword(v: String) = _registerForm.update { it.copy(password = v) }
    fun onRegisterConfirmar(v: String) = _registerForm.update { it.copy(confirmar = v) }

    // ---------- Acciones ----------

    fun login(onSuccess: () -> Unit) {
        _loginForm.update { it.copy(intentado = true) }
        val form = _loginForm.value
        if (!form.esValido || _state.value.cargando) return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val usuario = repository.login(form.email, form.password)
            if (usuario == null) {
                _state.update {
                    it.copy(cargando = false, error = "Correo o contraseña incorrectos")
                }
            } else {
                session.guardar(usuario.id)
                _state.update { it.copy(cargando = false, usuario = usuario) }
                limpiarFormularios()
                onSuccess()
            }
        }
    }

    fun registrar(onSuccess: () -> Unit) {
        _registerForm.update { it.copy(intentado = true) }
        val form = _registerForm.value
        if (!form.esValido || _state.value.cargando) return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val usuario = repository.registrar(form.nombre, form.email, form.password)
            if (usuario == null) {
                _state.update {
                    it.copy(cargando = false, error = "Este correo ya está registrado")
                }
            } else {
                session.guardar(usuario.id)
                _state.update { it.copy(cargando = false, usuario = usuario) }
                limpiarFormularios()
                onSuccess()
            }
        }
    }

    fun cerrarSesion(onDone: () -> Unit) {
        viewModelScope.launch {
            session.cerrar()
            _state.update { it.copy(usuario = null, error = null) }
            limpiarFormularios()
            onDone()
        }
    }

    fun limpiarError() {
        _state.update { it.copy(error = null) }
    }

    private fun limpiarFormularios() {
        _loginForm.value = LoginForm()
        _registerForm.value = RegisterForm()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TicketsApp
                AuthViewModel(app.usuarioRepository, app.sessionManager)
            }
        }
    }
}
