package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Insert
    suspend fun insertar(documento: Documento): Long

    @Delete
    suspend fun eliminar(documento: Documento)

    @Query("SELECT * FROM documentos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Documento>>
}