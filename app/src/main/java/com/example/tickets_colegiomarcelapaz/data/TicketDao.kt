package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insertar(ticket: Ticket): Long

    @Update
    suspend fun actualizar(ticket: Ticket)

    @Delete
    suspend fun eliminar(ticket: Ticket)

    /** Flow: Room vuelve a emitir la lista cada vez que la tabla cambia. */
    @Query("SELECT * FROM tickets WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun listarPorUsuario(usuarioId: Long): Flow<List<Ticket>>
}
