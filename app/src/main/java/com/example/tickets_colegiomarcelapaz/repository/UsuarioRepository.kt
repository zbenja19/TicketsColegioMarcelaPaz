package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.data.Usuario
import com.example.tickets_colegiomarcelapaz.data.UsuarioDao
import com.example.tickets_colegiomarcelapaz.remote.LoginRequest
import com.example.tickets_colegiomarcelapaz.remote.RegistroRequest
import com.example.tickets_colegiomarcelapaz.remote.UsuarioApiService
import com.example.tickets_colegiomarcelapaz.remote.UsuarioDto
import java.io.IOException
import retrofit2.HttpException

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val api: UsuarioApiService,
) {

    suspend fun registrar(nombre: String, email: String, password: String): Usuario {
        val creado = api.registrar(RegistroRequest(nombre.trim(), normalizar(email), password))
        return guardarEnCache(creado)
    }

    suspend fun login(email: String, password: String): Usuario {
        val usuario = api.login(LoginRequest(normalizar(email), password))
        return guardarEnCache(usuario)
    }

    suspend fun obtenerPorId(id: Long): Usuario? = try {
        guardarEnCache(api.buscar(id))
    } catch (e: IOException) {
        dao.buscarPorId(id)
    } catch (e: HttpException) {
        if (e.code() == 404) null else dao.buscarPorId(id)
    }

    private suspend fun guardarEnCache(dto: UsuarioDto): Usuario {
        val usuario = Usuario(id = dto.id, nombre = dto.nombre, email = dto.email)
        dao.guardar(usuario)
        return usuario
    }

    private fun normalizar(email: String) = email.trim().lowercase()
}
