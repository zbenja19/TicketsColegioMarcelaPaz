package com.example.tickets_colegiomarcelapaz.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tickets_colegiomarcelapaz.data.Documento
import com.example.tickets_colegiomarcelapaz.validation.TIPOS_DOCUMENTO
import com.example.tickets_colegiomarcelapaz.viewmodel.DocumentoViewModel
import java.io.File

@Composable
fun DocumentosScreen(
    onAbrirPdf: (Long) -> Unit,
    viewModel: DocumentoViewModel = viewModel(factory = DocumentoViewModel.Factory),
) {
    val documentos by viewModel.documentos.collectAsStateWithLifecycle()
    val subiendo by viewModel.subiendo.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var porEliminar by remember { mutableStateOf<Documento?>(null) }

    // Selector de archivos del sistema (recurso nativo). No necesita permisos:
    // el usuario elige el archivo y Android nos da acceso solo a ese.
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::subir)
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    fun abrir(documento: Documento) {
        if (documento.tipo == "application/pdf") {
            onAbrirPdf(documento.id)
            return
        }
        // Word: se abre con otra app, compartiendo el archivo de forma segura con FileProvider.
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(documento.ruta))
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, documento.tipo)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            viewModel.mostrarMensaje("No hay una app instalada para abrir Word")
        }
    }

    Box(Modifier.fillMaxSize()) {
        val lista = documentos
        when {
            lista == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            lista.isEmpty() -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Sin documentos", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Sube reglamentos, protocolos u otros PDF / Word del colegio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lista, key = { it.id }) { doc ->
                    DocumentoCard(
                        documento = doc,
                        onClick = { abrir(doc) },
                        onEliminar = { porEliminar = doc },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { if (!subiendo) selector.launch(TIPOS_DOCUMENTO) },
            icon = {
                if (subiendo) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else Icon(Icons.Filled.Add, contentDescription = null)
            },
            text = { Text(if (subiendo) "Subiendo..." else "Subir documento") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }

    porEliminar?.let { doc ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Eliminar \"${doc.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(doc); porEliminar = null }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun DocumentoCard(
    documento: Documento,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(documento.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = (if (documento.tipo == "application/pdf") "PDF" else "Word") +
                            " · " + Formatter.formatShortFileSize(context, documento.tamano),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar documento")
            }
        }
    }
}
