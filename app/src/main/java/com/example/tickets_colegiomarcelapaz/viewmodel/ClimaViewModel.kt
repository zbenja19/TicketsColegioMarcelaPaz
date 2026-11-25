package com.example.tickets_colegiomarcelapaz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tickets_colegiomarcelapaz.remote.RetrofitClient
import com.example.tickets_colegiomarcelapaz.repository.Clima
import com.example.tickets_colegiomarcelapaz.repository.ClimaRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface ClimaUiState {
    data object Cargando : ClimaUiState
    data class Exito(val clima: Clima) : ClimaUiState
    data object Error : ClimaUiState
}

class ClimaViewModel(private val repository: ClimaRepository) : ViewModel() {

    private val _estado = MutableStateFlow<ClimaUiState>(ClimaUiState.Cargando)
    val estado: StateFlow<ClimaUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = ClimaUiState.Cargando
            _estado.value = try {
                ClimaUiState.Exito(repository.climaColegio())
            } catch (e: IOException) {
                ClimaUiState.Error
            } catch (e: HttpException) {
                ClimaUiState.Error
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ClimaViewModel(ClimaRepository(RetrofitClient.climaApi))
            }
        }
    }
}