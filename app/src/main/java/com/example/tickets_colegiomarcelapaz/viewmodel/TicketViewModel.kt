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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista de tickets del usuario conectado (pantalla Inicio). */
@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModel(
    private val repository: TicketRepository,
    session: SessionManager,
) : ViewModel() {

    private val _filtroUrgencia = MutableStateFlow("Todas")
    val filtroUrgencia: StateFlow<String> = _filtroUrgencia.asStateFlow()

    private val ticketsRaw: StateFlow<List<Ticket>?> = session.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.tickets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Lista de tickets filtrados y/o ordenados por urgencia. */
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

    fun setFiltroUrgencia(filtro: String) {
        _filtroUrgencia.value = filtro
    }

    fun avanzarEstado(ticket: Ticket) {
        viewModelScope.launch { repository.avanzarEstado(ticket) }
    }

    fun eliminar(ticket: Ticket) {
        viewModelScope.launch { repository.eliminar(ticket) }
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
