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

class AuthViewModel(
    private val repository: UsuarioRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

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

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (_state.value.cargando) return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val usuario = repository.login(email, password)
            if (usuario == null) {
                _state.update {
                    it.copy(cargando = false, error = "Correo o contraseña incorrectos")
                }
            } else {
                session.guardar(usuario.id)
                _state.update { it.copy(cargando = false, usuario = usuario) }
                onSuccess()
            }
        }
    }

    fun registrar(nombre: String, email: String, password: String, onSuccess: () -> Unit) {
        if (_state.value.cargando) return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val usuario = repository.registrar(nombre, email, password)
            if (usuario == null) {
                _state.update {
                    it.copy(cargando = false, error = "Este correo ya está registrado")
                }
            } else {
                session.guardar(usuario.id)
                _state.update { it.copy(cargando = false, usuario = usuario) }
                onSuccess()
            }
        }
    }

    fun cerrarSesion(onDone: () -> Unit) {
        viewModelScope.launch {
            session.cerrar()
            _state.update { it.copy(usuario = null, error = null) }
            onDone()
        }
    }

    fun limpiarError() {
        _state.update { it.copy(error = null) }
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