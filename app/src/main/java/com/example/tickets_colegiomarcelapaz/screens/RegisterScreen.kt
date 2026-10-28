package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tickets_colegiomarcelapaz.viewmodel.RegisterForm

/** Vista de registro. El estado y la validación viven en AuthViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    form: RegisterForm,
    cargando: Boolean,
    errorMessage: String?,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmarChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
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
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Crea tu cuenta para reportar y dar seguimiento a los problemas del colegio.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            BannerError(errorMessage)

            CampoTexto(
                value = form.nombre,
                onValueChange = onNombreChange,
                label = "Nombre completo",
                error = form.nombreError,
                leadingIcon = Icons.Filled.Person,
            )

            CampoTexto(
                value = form.email,
                onValueChange = onEmailChange,
                label = "Correo electrónico",
                error = form.emailError,
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
            )

            CampoTexto(
                value = form.password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                error = form.passwordError,
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
            )

            CampoTexto(
                value = form.confirmar,
                onValueChange = onConfirmarChange,
                label = "Confirmar contraseña",
                error = form.confirmarError,
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )

            BotonPrincipal(texto = "Crear cuenta", cargando = cargando, onClick = submit)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) { Text("Inicia sesión") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            form = RegisterForm(intentado = true),
            cargando = false,
            errorMessage = null,
            onNombreChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmarChange = {},
            onSubmit = {},
            onNavigateToLogin = {},
            onBack = {},
        )
    }
}
