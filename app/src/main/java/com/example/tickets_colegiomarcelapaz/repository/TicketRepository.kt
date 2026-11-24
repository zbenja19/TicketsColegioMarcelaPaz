package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.data.TicketDao
import com.example.tickets_colegiomarcelapaz.remote.TicketApiService
import com.example.tickets_colegiomarcelapaz.remote.TicketDto
import com.example.tickets_colegiomarcelapaz.remote.TicketRequest
import java.io.File
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class TicketRepository(
    private val dao: TicketDao,
    private val api: TicketApiService,
) {

    fun tickets(usuarioId: Long) = dao.listarPorUsuario(usuarioId)

    suspend fun sincronizar(usuarioId: Long) {
        val remotos = api.listar(usuarioId)
        val fotos = dao.listarPorUsuario(usuarioId).first().associate { it.id to it.fotoRuta }
        dao.guardarTodos(remotos.map { it.aEntidad(fotos[it.id]) })
        dao.borrarAusentes(usuarioId, remotos.map { it.id })
    }

    suspend fun crear(ticket: Ticket): Long {
        val creado = api.crear(ticket.aRequest())
        dao.guardarTodos(listOf(creado.aEntidad(ticket.fotoRuta)))
        return creado.id
    }

    suspend fun avanzarEstado(ticket: Ticket) {
        val actualizado = api.avanzarEstado(ticket.id)
        dao.actualizar(actualizado.aEntidad(ticket.fotoRuta))
    }

    suspend fun eliminar(ticket: Ticket) {
        val respuesta = api.eliminar(ticket.id)
        if (!respuesta.isSuccessful && respuesta.code() != 404) throw HttpException(respuesta)
        dao.eliminar(ticket)
        ticket.fotoRuta?.let { File(it).delete() }
    }

    private fun TicketDto.aEntidad(fotoRuta: String?) = Ticket(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        descripcion = descripcion,
        sector = sector,
        urgencia = urgencia,
        estado = estado,
        fecha = fecha,
        fotoRuta = fotoRuta,
    )

    private fun Ticket.aRequest() = TicketRequest(
        usuarioId = usuarioId,
        titulo = titulo,
        descripcion = descripcion,
        sector = sector,
        urgencia = urgencia,
    )
}