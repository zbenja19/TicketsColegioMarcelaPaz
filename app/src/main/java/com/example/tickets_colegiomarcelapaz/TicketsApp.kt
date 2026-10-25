package com.example.tickets_colegiomarcelapaz

import android.app.Application
import androidx.room.Room
import com.example.tickets_colegiomarcelapaz.data.AppDatabase
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.repository.UsuarioRepository
import com.example.tickets_colegiomarcelapaz.repository.DocumentoRepository

class TicketsApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "tickets.db")
            // Solo durante el desarrollo: si cambias una entidad, recrea la BD.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val sessionManager: SessionManager by lazy { SessionManager(this) }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao())
    }

    val documentoRepository: DocumentoRepository by lazy {
        DocumentoRepository(this, database.documentoDao())
    }
}