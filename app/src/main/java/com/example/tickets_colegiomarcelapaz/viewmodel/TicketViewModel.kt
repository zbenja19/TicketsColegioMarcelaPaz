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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    /** null = todavía cargando. Se actualiza solo cuando Room detecta cambios. */
    val tickets: StateFlow<List<Ticket>?> = session.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.tickets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
