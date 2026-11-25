package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documentos",
    indices = [Index(value = ["usuarioId"])]
)
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val nombre: String,
    val tipo: String,
    val tamano: Long,
    val fecha: Long,
    val ruta: String,
    val categoria: String = "General",
)