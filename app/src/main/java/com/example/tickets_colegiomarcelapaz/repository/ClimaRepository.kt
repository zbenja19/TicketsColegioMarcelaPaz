package com.example.tickets_colegiomarcelapaz.repository

import com.example.tickets_colegiomarcelapaz.remote.ClimaApiService
import kotlin.math.roundToInt

data class Clima(
    val temperatura: Int,
    val descripcion: String,
    val probabilidadLluvia: Int,
) {
    val alertaLluvia: Boolean get() = probabilidadLluvia >= 50
}

class ClimaRepository(private val api: ClimaApiService) {

    suspend fun climaColegio(): Clima {
        val respuesta = api.obtenerClima(LATITUD, LONGITUD)
        return Clima(
            temperatura = respuesta.current.temperatura.roundToInt(),
            descripcion = describirClima(respuesta.current.codigo),
            probabilidadLluvia = respuesta.daily.probabilidadLluvia.firstOrNull() ?: 0,
        )
    }

    companion object {
        const val LATITUD = -33.45
        const val LONGITUD = -70.66
    }
}

fun describirClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1, 2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    in 51..57 -> "Llovizna"
    in 61..67, in 80..82 -> "Lluvia"
    in 71..77, 85, 86 -> "Nieve"
    in 95..99 -> "Tormenta"
    else -> "Sin datos"
}