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
    val tipo: String,        // MIME: application/pdf, etc.
    val tamano: Long,        // bytes
    val fecha: Long,         // System.currentTimeMillis()
    val ruta: String,        // ruta del archivo copiado en filesDir/documentos
    val categoria: String = "General",
)