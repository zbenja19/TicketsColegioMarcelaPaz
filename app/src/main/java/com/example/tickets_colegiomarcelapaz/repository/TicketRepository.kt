package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.model.Ticket

class TicketRepository{
    fun obtenerTickets(): List<Ticket> = listOf (
        Ticket(1, "Proyector sin imagen", "Sala 12 no proyecta", "Abierto"),
        Ticket(2, "Proyector sin imagen", "Sala 12 no proyecta", "En Proceso"),
        Ticket(3, "Proyector sin imagen", "Sala 12 no proyecta", "Cerrado")

    )
}