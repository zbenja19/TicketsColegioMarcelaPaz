package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tickets_colegiomarcelapaz.viewmodel.DocumentoViewModel

/** Estado del visor: cargando, error o páginas listas. */
private sealed interface PdfEstado {
    data object Cargando : PdfEstado
    data object Error : PdfEstado
    data class Listo(val nombre: String, val paginas: List<ImageBitmap>) : PdfEstado
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    documentoId: Long,
    onBack: () -> Unit,
    viewModel: DocumentoViewModel = viewModel(factory = DocumentoViewModel.Factory),
) {
    val estado by produceState<PdfEstado>(PdfEstado.Cargando, documentoId) {
        value = try {
            val doc = viewModel.buscar(documentoId)
            if (doc == null) PdfEstado.Error
            else PdfEstado.Listo(doc.nombre, viewModel.paginasPdf(doc.ruta).map { it.asImageBitmap() })
        } catch (e: Exception) {
            PdfEstado.Error // PDF dañado o protegido con contraseña
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (estado as? PdfEstado.Listo)?.nombre ?: "Documento",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (val e = estado) {
                PdfEstado.Cargando -> CircularProgressIndicator()
                PdfEstado.Error -> Text("No se pudo abrir el PDF", style = MaterialTheme.typography.titleMedium)
                is PdfEstado.Listo -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(e.paginas) { pagina ->
                        Image(
                            bitmap = pagina,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
