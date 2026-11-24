package com.example.tickets_colegiomarcelapaz.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL_TICKETS = "http://10.0.2.2:8082/"

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val ticketApi: TicketApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_TICKETS)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TicketApiService::class.java)
    }
}