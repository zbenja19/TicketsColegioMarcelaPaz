package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, Documento::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
}