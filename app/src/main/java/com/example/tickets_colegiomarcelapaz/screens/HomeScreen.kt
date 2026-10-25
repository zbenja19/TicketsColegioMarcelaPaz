package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    nombre: String,
    onLogout: () -> Unit,
) {
    var confirmar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { confirmar = true }) { Text("Cerrar sesión") }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar tu sesión?") },
            confirmButton = {
                TextButton(onClick = { confirmar = false; onLogout() }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar") }
            },
        )
    }
}