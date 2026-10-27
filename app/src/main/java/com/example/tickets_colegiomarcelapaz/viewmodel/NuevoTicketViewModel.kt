package com.example.tickets_colegiomarcelapaz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tickets_colegiomarcelapaz.TicketsApp
import com.example.tickets_colegiomarcelapaz.data.SessionManager
import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.repository.TicketRepository
import com.example.tickets_colegiomarcelapaz.validation.validateDescription
import com.example.tickets_colegiomarcelapaz.validation.validatePlace
import com.example.tickets_colegiomarcelapaz.validation.validateSector
import com.example.tickets_colegiomarcelapaz.validation.validateTitle
import com.example.tickets_colegiomarcelapaz.validation.validateUrgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class NuevoTicketForm(
    val titulo: String = "",
    val descripcion: String = "",
    val sector: String = "",
    val lugarOtro: String = "",
    val urgencia: String = "",
    val fotoRuta: String? = null,
    val intentado: Boolean = false,
    val guardando: Boolean = false,
) {
    val tituloError get() = if (intentado) validateTitle(titulo) else null
    val descripcionError get() = if (intentado) validateDescription(descripcion) else null
    val sectorError get() = if (intentado) validateSector(sector) else null
    val lugarOtroError get() = if (intentado) validatePlace(sector, lugarOtro) else null
    val urgenciaError get() = if (intentado) validateUrgency(urgencia) else null
    val esValido
        get() = validateTitle(titulo) == null &&
                validateDescription(descripcion) == null &&
                validateSector(sector) == null &&
                validatePlace(sector, lugarOtro) == null &&
                validateUrgency(urgencia) == null
}

class NuevoTicketViewModel(
    private val repository: TicketRepository,
    private val session: SessionManager,
) : ViewModel() {

    private val _form = MutableStateFlow(NuevoTicketForm())
    val form: StateFlow<NuevoTicketForm> = _form.asStateFlow()

    private var guardado = false

    fun onTitulo(v: String) = _form.update { it.copy(titulo = v) }
    fun onDescripcion(v: String) = _form.update { it.copy(descripcion = v) }
    fun onLugarOtro(v: String) = _form.update { it.copy(lugarOtro = v) }
    fun onUrgencia(v: String) = _form.update { it.copy(urgencia = v) }

    /** Si cambia a un sector distinto de "Otro", se limpia el lugar exacto. */
    fun onSector(v: String) = _form.update {
        it.copy(sector = v, lugarOtro = if (v == "Otro") it.lugarOtro else "")
    }

    /** Guarda la ruta de la nueva foto (o null para quitarla) y borra la anterior. */
    fun onFoto(ruta: String?) {
        _form.value.fotoRuta?.takeIf { it != ruta }?.let { File(it).delete() }
        _form.update { it.copy(fotoRuta = ruta) }
    }

    fun guardar(onExito: () -> Unit) {
        _form.update { it.copy(intentado = true) }
        val f = _form.value
        if (!f.esValido || f.guardando) return
        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            val usuarioId = session.usuarioId.first()
            if (usuarioId == null) {
                _form.update { it.copy(guardando = false) }
                return@launch
            }
            repository.crear(
                Ticket(
                    usuarioId = usuarioId,
                    titulo = f.titulo.trim(),
                    descripcion = f.descripcion.trim(),
                    sector = if (f.sector == "Otro") f.lugarOtro.trim() else f.sector,
                    urgencia = f.urgencia,
                    fecha = System.currentTimeMillis(), // fecha automática
                    fotoRuta = f.fotoRuta,
                )
            )
            guardado = true
            onExito()
        }
    }

    /** Si el usuario sale sin guardar, la foto tomada ya no sirve: se borra. */
    override fun onCleared() {
        if (!guardado) _form.value.fotoRuta?.let { File(it).delete() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TicketsApp
                NuevoTicketViewModel(app.ticketRepository, app.sessionManager)
            }
        }
    }
}
