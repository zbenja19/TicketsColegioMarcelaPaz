package com.example.tickets_colegiomarcelapaz

import android.app.Application
import androidx.room.Room
import com.example.tickets_colegiomarcelapaz.data.AppDatabase
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.repository.DocumentoRepository
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import com.example.tickets_colegiomarcelapaz.repository.UsuarioRepository

/** Crea una sola vez la BD y los repositorios; los ViewModel los piden desde sus Factory. */
class TicketsApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "tickets.db")
            // Si cambia la versión de la BD, se recrea (se pierden los datos de prueba).
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

    val ticketRepository: TicketRepository by lazy {
        TicketRepository(database.ticketDao())
    }
}
