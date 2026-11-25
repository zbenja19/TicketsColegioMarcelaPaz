package com.example.tickets_colegiomarcelapaz.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UsuarioDao {
    @Upsert
    suspend fun guardar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?
}
