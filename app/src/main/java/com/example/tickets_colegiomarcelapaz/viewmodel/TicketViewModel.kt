package com.example.tickets_colegiomarcelapaz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tickets_colegiomarcelapaz.model.Ticket
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository

class TicketViewModel (
    private val repository: TicketRepository = TicketRepository()
) : ViewModel() {
    var tickets by mutableStateOf<List<Ticket>>(emptyList())
    private set

    init{
        cargarTickets()
    }

    private fun cargarTickets() {
        tickets = repository.obtenerTickets()
    }
}


