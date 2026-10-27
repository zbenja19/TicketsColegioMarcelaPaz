package com.example.tickets_colegiomarcelapaz.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


internal fun validateName(value: String): String? = when {
    value.isBlank() -> "Ingresa tu nombre"
    value.trim().length < 3 -> "Debe tener al menos 3 caracteres"
    else -> null
}

internal fun validateEmail(value: String): String? = when {
    value.isBlank() -> "Ingresa tu correo"
    !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> "Correo no válido"
    else -> null
}

/** [strict] = true para registro (reglas de seguridad); false para login. */
internal fun validatePassword(value: String, strict: Boolean): String? = when {
    value.isBlank() -> "Ingresa tu contraseña"
    strict && value.length < 8 -> "Mínimo 8 caracteres"
    strict && (value.none { it.isLetter() } || value.none { it.isDigit() }) ->
        "Debe incluir letras y números"
    else -> null
}

internal fun validateConfirmPassword(password: String, confirm: String): String? = when {
    confirm.isBlank() -> "Confirma tu contraseña"
    password != confirm -> "Las contraseñas no coinciden"
    else -> null
}

// ---------- Campo de texto reutilizable (Material 3) ----------

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Ocultar" else "Mostrar")
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
    )
}