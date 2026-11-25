package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tickets_colegiomarcelapaz.data.Ticket
import com.example.tickets_colegiomarcelapaz.viewmodel.TicketViewModel
import java.text.DateFormat
import java.util.Date
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import com.example.tickets_colegiomarcelapaz.viewmodel.ClimaUiState
import com.example.tickets_colegiomarcelapaz.viewmodel.ClimaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nombre: String,
    onNuevoTicket: () -> Unit,
    viewModel: TicketViewModel = viewModel(factory = TicketViewModel.Factory),
    climaViewModel: ClimaViewModel = viewModel(factory = ClimaViewModel.Factory),
) {
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val filtroActual by viewModel.filtroUrgencia.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val clima by climaViewModel.estado.collectAsStateWithLifecycle()
    var porEliminar by remember { mutableStateOf<Ticket?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Hola, $nombre",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Tienes ${tickets?.size ?: 0} reporte(s). Toca el estado para avanzarlo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TarjetaClima(estado = clima, onReintentar = climaViewModel::cargar)
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Filtrar por prioridad:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val opciones = listOf("Todas", "Alta", "Media", "Baja", "Alta a Baja")
                    items(opciones) { opcion ->
                        val seleccionado = filtroActual == opcion
                        FilterChip(
                            selected = seleccionado,
                            onClick = { viewModel.setFiltroUrgencia(opcion) },
                            label = { Text(opcion) },
                            leadingIcon = if (seleccionado) {
                                { Icon(Icons.Filled.Check, contentDescription = null) }
                            } else null,
                        )
                    }
                }
            }

            error?.let { mensaje ->
                ErrorServidor(mensaje = mensaje, onReintentar = viewModel::recargar)
            }

            PullToRefreshBox(
                isRefreshing = cargando,
                onRefresh = {
                    viewModel.recargar()
                    climaViewModel.cargar()
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                val lista = tickets
                when {
                    lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    lista.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (filtroActual == "Todas") {
                                Text("Aún no tienes reportes", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Toca \"Nuevo reporte\" para crear el primero.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            } else {
                                Text("Sin reportes", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "No hay reportes para el filtro \"$filtroActual\".",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(lista, key = { it.id }) { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onEstado = { viewModel.avanzarEstado(ticket) },
                                onEliminar = { porEliminar = ticket },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevoTicket,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo reporte") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    porEliminar?.let { ticket ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar reporte") },
            text = { Text("¿Eliminar \"${ticket.titulo}\"? No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(ticket); porEliminar = null }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun ErrorServidor(mensaje: String, onReintentar: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onReintentar) { Text("Reintentar") }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: Ticket,
    onEstado: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ticket.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                EstadoChip(ticket.estado, onClick = onEstado)
            }
            Text(
                text = ticket.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ticket.fotoRuta?.let {
                FotoEvidencia(
                    ruta = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${ticket.sector} · ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                UrgenciaBadge(urgencia = ticket.urgencia)
                Text(
                    text = " · " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(ticket.fecha)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar reporte")
                }
            }
        }
    }
}

@Composable
private fun UrgenciaBadge(urgencia: String) {
    val (fondo, texto) = when (urgencia.lowercase()) {
        "alta" -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        "media" -> Color(0xFFFFE0B2) to Color(0xFFE65100)
        "baja" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = fondo,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = urgencia,
            color = texto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EstadoChip(estado: String, onClick: () -> Unit) {
    val (fondo, texto) = when (estado) {
        "Abierto" -> Color(0xFFFFE0B2) to Color(0xFF7A4100)
        "En Proceso" -> Color(0xFFBBDEFB) to Color(0xFF0D3C73)
        "Cerrado" -> Color(0xFFC8E6C9) to Color(0xFF1B5E20)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val fondoAnimado by animateColorAsState(fondo, label = "fondoEstado")
    val textoAnimado by animateColorAsState(texto, label = "textoEstado")

    Surface(
        color = fondoAnimado,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = estado,
            color = textoAnimado,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TarjetaClima(estado: ClimaUiState, onReintentar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (estado) {
                ClimaUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = "Consultando el clima del colegio...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
                ClimaUiState.Error -> {
                    Text(
                        text = "No se pudo obtener el clima",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onReintentar) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reintentar clima")
                    }
                }
                is ClimaUiState.Exito -> {
                    val datos = estado.clima
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${datos.temperatura}°C · ${datos.descripcion}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Probabilidad de lluvia hoy: ${datos.probabilidadLluvia}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (datos.alertaLluvia) {
                            Text(
                                text = "Revisar techos, canaletas y posibles filtraciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    if (datos.alertaLluvia) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Alerta de lluvia",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}