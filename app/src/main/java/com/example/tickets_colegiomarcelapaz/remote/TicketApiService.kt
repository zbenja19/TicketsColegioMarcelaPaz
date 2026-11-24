package com.example.tickets_colegiomarcelapaz.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketApiService {

    @GET("api/tickets")
    suspend fun listar(@Query("usuarioId") usuarioId: Long): List<TicketDto>

    @GET("api/tickets/{id}")
    suspend fun buscar(@Path("id") id: Long): TicketDto

    @POST("api/tickets")
    suspend fun crear(@Body request: TicketRequest): TicketDto

    @PUT("api/tickets/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body request: TicketRequest): TicketDto

    @PATCH("api/tickets/{id}/estado")
    suspend fun avanzarEstado(@Path("id") id: Long): TicketDto

    @DELETE("api/tickets/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}