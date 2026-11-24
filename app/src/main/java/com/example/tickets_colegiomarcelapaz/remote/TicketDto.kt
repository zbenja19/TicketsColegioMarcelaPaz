package com.example.tickets_colegiomarcelapaz.remote

data class TicketDto(
    val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val descripcion: String,
    val sector: String,
    val urgencia: String,
    val estado: String,
    val fecha: Long,
)

data class TicketRequest(
    val usuarioId: Long,
    val titulo: String,
    val descripcion: String,
    val sector: String,
    val urgencia: String,
)