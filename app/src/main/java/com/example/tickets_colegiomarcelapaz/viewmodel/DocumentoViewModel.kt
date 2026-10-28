package com.example.tickets_colegiomarcelapaz.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tickets_colegiomarcelapaz.TicketsApp
import com.example.tickets_colegiomarcelapaz.data.Documento
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.repository.DocumentoRepository
import com.example.tickets_colegiomarcelapaz.validation.validateDocument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentoViewModel(
    private val repository: DocumentoRepository,
    private val session: SessionManager,
) : ViewModel() {

    /** null = cargando. */
    val documentos: StateFlow<List<Documento>?> = session.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.documentos(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _subiendo = MutableStateFlow(false)
    val subiendo: StateFlow<Boolean> = _subiendo.asStateFlow()

    /** Mensaje para el Snackbar; la UI llama a [mensajeMostrado] después de mostrarlo. */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun subir(uri: Uri) {
        viewModelScope.launch {
            val usuarioId = session.usuarioId.first() ?: return@launch
            _subiendo.value = true
            try {
                val info = repository.leerInfo(uri)
                val error = validateDocument(info.tipo, info.tamano)
                if (error != null) {
                    _mensaje.value = error
                } else {
                    repository.guardar(uri, info, usuarioId)
                    _mensaje.value = "Documento agregado"
                }
            } catch (e: Exception) {
                _mensaje.value = "No se pudo copiar el archivo"
            } finally {
                _subiendo.value = false
            }
        }
    }

    fun eliminar(documento: Documento) {
        viewModelScope.launch {
            repository.eliminar(documento)
            _mensaje.value = "Documento eliminado"
        }
    }

    fun mostrarMensaje(texto: String) {
        _mensaje.value = texto
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    suspend fun buscar(id: Long): Documento? = repository.buscar(id)

    suspend fun paginasPdf(ruta: String): List<Bitmap> = repository.renderizarPdf(ruta)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TicketsApp
                DocumentoViewModel(app.documentoRepository, app.sessionManager)
            }
        }
    }
}
