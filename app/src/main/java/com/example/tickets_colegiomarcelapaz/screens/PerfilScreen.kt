package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerfilScreen(nombre: String, email: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar con la inicial del nombre
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = nombre.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(nombre, style = MaterialTheme.typography.headlineSmall)
        Text(email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Cerrar sesión")
        }
    }
}
