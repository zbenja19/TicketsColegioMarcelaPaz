package com.example.tickets_colegiomarcelapaz.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tickets_colegiomarcelapaz.validation.SECTORES
import com.example.tickets_colegiomarcelapaz.validation.URGENCIAS
import com.example.tickets_colegiomarcelapaz.viewmodel.NuevoTicketViewModel
import java.io.File
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoTicketScreen(
    onBack: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: NuevoTicketViewModel = viewModel(factory = NuevoTicketViewModel.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ---------- Cámara (recurso nativo) ----------
    // 1) Se crea un archivo vacío en filesDir/fotos y se comparte con la app de cámara vía FileProvider.
    // 2) La cámara escribe la foto ahí; si el usuario cancela, se borra el archivo.
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }
    var camaraNoDisponible by remember { mutableStateOf(false) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        if (exito && ruta != null) viewModel.onFoto(ruta) else ruta?.let { File(it).delete() }
        rutaPendiente = null
    }

    fun abrirCamara() {
        val (ruta, uri) = crearArchivoFoto(context)
        rutaPendiente = ruta
        try {
            camara.launch(uri)
        } catch (e: ActivityNotFoundException) {
            camaraNoDisponible = true
        }
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        if (concedido) abrirCamara()
    }

    fun tomarFoto() {
        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (tienePermiso) abrirCamara() else pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo reporte") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CampoTexto(
                value = form.titulo,
                onValueChange = viewModel::onTitulo,
                label = "Título",
                error = form.tituloError,
                leadingIcon = Icons.Filled.Edit,
            )

            CampoTexto(
                value = form.descripcion,
                onValueChange = viewModel::onDescripcion,
                label = "Descripción del problema",
                error = form.descripcionError,
                leadingIcon = Icons.Filled.Info,
                singleLine = false,
            )

            SelectorSector(
                sector = form.sector,
                error = form.sectorError,
                onSector = viewModel::onSector,
            )

            // El campo "lugar exacto" aparece/desaparece con animación solo si el sector es "Otro".
            AnimatedVisibility(visible = form.sector == "Otro") {
                CampoTexto(
                    value = form.lugarOtro,
                    onValueChange = viewModel::onLugarOtro,
                    label = "Nombre del lugar exacto",
                    error = form.lugarOtroError,
                    leadingIcon = Icons.Filled.Place,
                )
            }

            Text("Nivel de urgencia", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                URGENCIAS.forEach { nivel ->
                    val seleccionado = form.urgencia == nivel
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.onUrgencia(nivel) },
                        label = { Text(nivel) },
                        leadingIcon = if (seleccionado) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null,
                    )
                }
            }
            form.urgenciaError?.let { MensajeError(it) }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = "Fecha: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date()) +
                            " (se registra al guardar)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Foto de evidencia (opcional)", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { tomarFoto() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(if (form.fotoRuta == null) "Tomar foto" else "Tomar otra foto")
            }
            if (permisoDenegado) MensajeError("Sin permiso de cámara no se puede tomar la foto")
            if (camaraNoDisponible) MensajeError("Este dispositivo no tiene app de cámara")

            AnimatedVisibility(visible = form.fotoRuta != null) {
                Column(horizontalAlignment = Alignment.End) {
                    form.fotoRuta?.let {
                        FotoEvidencia(
                            ruta = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    }
                    TextButton(onClick = { viewModel.onFoto(null) }) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Quitar foto")
                    }
                }
            }

            BotonPrincipal(texto = "Guardar reporte", cargando = form.guardando) {
                focusManager.clearFocus()
                viewModel.guardar(onExito = onGuardado)
            }
        }
    }
}

/** Menú desplegable de sectores. Un Box transparente encima del campo abre el menú al tocarlo. */
@Composable
private fun SelectorSector(sector: String, error: String?, onSector: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = sector,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sector del colegio") },
            leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Abrir lista") },
            isError = error != null,
            supportingText = { if (error != null) MensajeError(error) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { abierto = true }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            SECTORES.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSector(opcion)
                        abierto = false
                    },
                )
            }
        }
    }
}

/** Crea filesDir/fotos/ticket_<hora>.jpg y devuelve su ruta y un content:// para la cámara. */
private fun crearArchivoFoto(context: Context): Pair<String, Uri> {
    val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
    val archivo = File(carpeta, "ticket_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    return archivo.absolutePath to uri
}
