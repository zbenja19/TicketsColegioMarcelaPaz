package com.example.tickets_colegiomarcelapaz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tickets_colegiomarcelapaz.TicketsApp
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModel(
    private val repository: TicketRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _filtroUrgencia = MutableStateFlow("Todas")
    val filtroUrgencia: StateFlow<String> = _filtroUrgencia.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val ticketsRaw: StateFlow<List<Ticket>?> = session.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.tickets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tickets: StateFlow<List<Ticket>?> = combine(
        ticketsRaw,
        _filtroUrgencia,
    ) { lista, filtro ->
        lista?.let { ticketsList ->
            when (filtro) {
                "Alta" -> ticketsList.filter { it.urgencia.equals("Alta", ignoreCase = true) }
                "Media" -> ticketsList.filter { it.urgencia.equals("Media", ignoreCase = true) }
                "Baja" -> ticketsList.filter { it.urgencia.equals("Baja", ignoreCase = true) }
                "Alta a Baja" -> ticketsList.sortedBy { ticket ->
                    when (ticket.urgencia.lowercase()) {
                        "alta" -> 1
                        "media" -> 2
                        "baja" -> 3
                        else -> 4
                    }
                }
                else -> ticketsList
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        recargar()
    }

    fun setFiltroUrgencia(filtro: String) {
        _filtroUrgencia.value = filtro
    }

    fun recargar() {
        viewModelScope.launch {
            val usuarioId = session.usuarioId.first() ?: return@launch
            ejecutar { repository.sincronizar(usuarioId) }
        }
    }

    fun avanzarEstado(ticket: Ticket) {
        viewModelScope.launch { ejecutar { repository.avanzarEstado(ticket) } }
    }

    fun eliminar(ticket: Ticket) {
        viewModelScope.launch { ejecutar { repository.eliminar(ticket) } }
    }

    fun limpiarError() {
        _error.value = null
    }

    private suspend fun ejecutar(accion: suspend () -> Unit) {
        _cargando.value = true
        _error.value = null
        try {
            accion()
        } catch (e: IOException) {
            _error.value = "Sin conexión con el servidor. Revisa que esté encendido."
        } catch (e: HttpException) {
            _error.value = "Error del servidor (${e.code()})."
        } finally {
            _cargando.value = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TicketsApp
                TicketViewModel(app.ticketRepository, app.sessionManager)
            }
        }
    }
}