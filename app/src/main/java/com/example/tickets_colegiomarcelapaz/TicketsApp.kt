package com.example.tickets_colegiomarcelapaz

import android.app.Application
import androidx.room.Room
import com.example.tickets_colegiomarcelapaz.data.AppDatabase
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.remote.RetrofitClient
import com.example.tickets_colegiomarcelapaz.repository.DocumentoRepository
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import com.example.tickets_colegiomarcelapaz.repository.UsuarioRepository

class TicketsApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "tickets.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val sessionManager: SessionManager by lazy { SessionManager(this) }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao(), RetrofitClient.usuarioApi)
    }

    val documentoRepository: DocumentoRepository by lazy {
        DocumentoRepository(this, database.documentoDao())
    }

    val ticketRepository: TicketRepository by lazy {
        TicketRepository(database.ticketDao(), RetrofitClient.ticketApi)
    }
}