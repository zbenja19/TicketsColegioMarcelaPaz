package com.example.tickets_colegiomarcelapaz.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
)

data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

interface UsuarioApiService {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body request: RegistroRequest): UsuarioDto

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): UsuarioDto

    @GET("api/usuarios/{id}")
    suspend fun buscar(@Path("id") id: Long): UsuarioDto
}
