package com.example.tickets_colegiomarcelapaz.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL_TICKETS = "http://10.0.2.2:8082/"
    private const val BASE_URL_CLIMA = "https://api.open-meteo.com/"

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun crear(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val ticketApi: TicketApiService by lazy {
        crear(BASE_URL_TICKETS).create(TicketApiService::class.java)
    }

    val climaApi: ClimaApiService by lazy {
        crear(BASE_URL_CLIMA).create(ClimaApiService::class.java)
    }
}