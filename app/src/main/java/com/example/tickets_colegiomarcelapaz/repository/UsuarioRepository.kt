package com.example.tickets_colegiomarcelapaz.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.tickets_colegiomarcelapaz.data.Usuario
import com.example.tickets_colegiomarcelapaz.data.UsuarioDao
import java.security.MessageDigest

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun registrar(nombre: String, email: String, password: String): Usuario? {
        val correo = email.trim().lowercase()
        if (dao.buscarPorEmail(correo) != null) return null
        val nuevo = Usuario(nombre = nombre.trim(), email = correo, passwordHash = hash(password))
        return try {
            nuevo.copy(id = dao.insertar(nuevo))
        } catch (e: SQLiteConstraintException) {
            null
        }
    }

    suspend fun login(email: String, password: String): Usuario? {
        val usuario = dao.buscarPorEmail(email.trim().lowercase()) ?: return null
        return if (usuario.passwordHash == hash(password)) usuario else null
    }

    suspend fun obtenerPorId(id: Long): Usuario? = dao.buscarPorId(id)

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
}