package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tickets",
    indices = [Index(value = ["usuarioId"])]
)
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val descripcion: String,
    val sector: String,
    val urgencia: String,
    val estado: String = ESTADOS.first(),
    val fecha: Long,
    val fotoRuta: String? = null,
)

val ESTADOS = listOf("Abierto", "En Proceso", "Cerrado")

fun siguienteEstado(estado: String): String =
    ESTADOS[(ESTADOS.indexOf(estado) + 1) % ESTADOS.size]
