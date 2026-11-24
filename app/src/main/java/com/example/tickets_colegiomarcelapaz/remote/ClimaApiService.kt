package com.example.tickets_colegiomarcelapaz.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ClimaResponse(
    val current: ClimaActual,
    val daily: ClimaDiario,
)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("weather_code") val codigo: Int,
)

data class ClimaDiario(
    @SerializedName("precipitation_probability_max") val probabilidadLluvia: List<Int>,
)

interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") actual: String = "temperature_2m,weather_code",
        @Query("daily") diario: String = "precipitation_probability_max",
        @Query("forecast_days") dias: Int = 1,
        @Query("timezone") zonaHoraria: String = "America/Santiago",
    ): ClimaResponse
}