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
    val urgencia: String,     // Baja, Media o Alta
    val estado: String = ESTADOS.first(),
    val fecha: Long,          // System.currentTimeMillis() al guardar
    val fotoRuta: String? = null,
)

val ESTADOS = listOf("Abierto", "En Proceso", "Cerrado")

/** Abierto -> En Proceso -> Cerrado -> Abierto. */
fun siguienteEstado(estado: String): String =
    ESTADOS[(ESTADOS.indexOf(estado) + 1) % ESTADOS.size]
