package com.example.tickets_colegiomarcelapaz.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    private val keyUsuarioId = longPreferencesKey("usuario_id")

    /** Id del usuario conectado, o null si no hay sesión. */
    val usuarioId: Flow<Long?> = context.dataStore.data.map { it[keyUsuarioId] }

    suspend fun guardar(id: Long) {
        context.dataStore.edit { it[keyUsuarioId] = id }
    }

    suspend fun cerrar() {
        context.dataStore.edit { it.remove(keyUsuarioId) }
    }
}