package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.data.TicketDao
import com.example.tickets_colegiomarcelapaz.data.siguienteEstado
import java.io.File

class TicketRepository(private val dao: TicketDao) {

    fun tickets(usuarioId: Long) = dao.listarPorUsuario(usuarioId)

    suspend fun crear(ticket: Ticket): Long = dao.insertar(ticket)

    suspend fun avanzarEstado(ticket: Ticket) =
        dao.actualizar(ticket.copy(estado = siguienteEstado(ticket.estado)))

    /** Borra la fila y también la foto, para no dejar archivos huérfanos. */
    suspend fun eliminar(ticket: Ticket) {
        dao.eliminar(ticket)
        ticket.fotoRuta?.let { File(it).delete() }
    }
}
