package com.example.tickets_colegiomarcelapaz.viewmodel

import com.example.tickets_colegiomarcelapaz.MainDispatcherRule
import com.example.tickets_colegiomarcelapaz.remote.ClimaActual
import com.example.tickets_colegiomarcelapaz.remote.ClimaApiService
import com.example.tickets_colegiomarcelapaz.remote.ClimaDiario
import com.example.tickets_colegiomarcelapaz.remote.ClimaResponse
import com.example.tickets_colegiomarcelapaz.repository.Clima
import com.example.tickets_colegiomarcelapaz.repository.ClimaRepository
import com.example.tickets_colegiomarcelapaz.repository.describirClima
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ClimaTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = mockk<ClimaApiService>()
    private val repository = mockk<ClimaRepository>()

    @Test
    fun `describirClima traduce los codigos WMO`() {
        assertEquals("Despejado", describirClima(0))
        assertEquals("Nublado", describirClima(3))
        assertEquals("Lluvia", describirClima(63))
        assertEquals("Lluvia", describirClima(81))
        assertEquals("Tormenta", describirClima(95))
        assertEquals("Sin datos", describirClima(999))
    }

    @Test
    fun `el repositorio convierte la respuesta de Open-Meteo en Clima`() = runTest {
        coEvery { api.obtenerClima(any(), any()) } returns
                ClimaResponse(ClimaActual(18.6, 61), ClimaDiario(listOf(80)))

        val clima = ClimaRepository(api).climaColegio()

        assertEquals(Clima(temperatura = 19, descripcion = "Lluvia", probabilidadLluvia = 80), clima)
        assertTrue(clima.alertaLluvia)
    }

    @Test
    fun `sin datos diarios la probabilidad de lluvia es cero y no hay alerta`() = runTest {
        coEvery { api.obtenerClima(any(), any()) } returns
                ClimaResponse(ClimaActual(22.0, 0), ClimaDiario(emptyList()))

        val clima = ClimaRepository(api).climaColegio()

        assertEquals(0, clima.probabilidadLluvia)
        assertFalse(clima.alertaLluvia)
    }

    @Test
    fun `el viewmodel muestra Exito cuando la API responde`() = runTest {
        val clima = Clima(24, "Despejado", 6)
        coEvery { repository.climaColegio() } returns clima

        val viewModel = ClimaViewModel(repository)

        assertEquals(ClimaUiState.Exito(clima), viewModel.estado.value)
    }

    @Test
    fun `el viewmodel muestra Error sin conexion y se recupera al reintentar`() = runTest {
        coEvery { repository.climaColegio() } throws IOException("sin red")
        val viewModel = ClimaViewModel(repository)
        assertEquals(ClimaUiState.Error, viewModel.estado.value)

        val clima = Clima(24, "Despejado", 6)
        coEvery { repository.climaColegio() } returns clima
        viewModel.cargar()

        assertEquals(ClimaUiState.Exito(clima), viewModel.estado.value)
    }
}