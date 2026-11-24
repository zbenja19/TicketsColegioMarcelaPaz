package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insertar(ticket: Ticket): Long

    @Update
    suspend fun actualizar(ticket: Ticket)

    @Delete
    suspend fun eliminar(ticket: Ticket)

    @Upsert
    suspend fun guardarTodos(tickets: List<Ticket>)

    @Query("DELETE FROM tickets WHERE usuarioId = :usuarioId AND id NOT IN (:ids)")
    suspend fun borrarAusentes(usuarioId: Long, ids: List<Long>)

    @Query("SELECT * FROM tickets WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun listarPorUsuario(usuarioId: Long): Flow<List<Ticket>>
}