package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey val id: Long,
    val nombre: String,
    val email: String,
)
