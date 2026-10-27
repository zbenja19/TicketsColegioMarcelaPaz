package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, Documento::class, Ticket::class],
    version = 2, // 2: se agregó la tabla tickets
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun ticketDao(): TicketDao
}
