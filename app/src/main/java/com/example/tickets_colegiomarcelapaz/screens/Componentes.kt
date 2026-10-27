package com.example.tickets_colegiomarcelapaz.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------- Mensaje de error con ícono (se usa debajo de cada campo) ----------

@Composable
internal fun MensajeError(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp),
        )
        Text(texto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

// ---------- Campo de texto reutilizable (Material 3) ----------

@Composable
internal fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
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
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = error != null,
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        supportingText = { if (error != null) MensajeError(error) },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = when {
            isPassword -> {
                {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Ocultar" else "Mostrar")
                    }
                }
            }
            error != null -> {
                { Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
            }
            else -> null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
    )
}

// ---------- Banner de error animado (login / registro) ----------

@Composable
internal fun BannerError(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Text(
                text = mensaje ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

// ---------- Botón principal que muestra un loader mientras [cargando] ----------

@Composable
internal fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary,
            )
        } else {
            Text(texto, fontSize = 17.sp, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ---------- Foto de evidencia con borde redondeado ----------

/** Carga la foto en segundo plano y la muestra recortada con esquinas redondeadas. */
@Composable
internal fun FotoEvidencia(ruta: String, modifier: Modifier = Modifier) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { cargarFoto(ruta, maxLado = 1024)?.asImageBitmap() }
    }
    val forma = RoundedCornerShape(16.dp)
    imagen?.let {
        Image(
            bitmap = it,
            contentDescription = "Foto de evidencia",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(forma)
                .border(2.dp, MaterialTheme.colorScheme.secondary, forma),
        )
    }
}

/** Reduce la foto (inSampleSize) para no cargar 12 MP en memoria y la endereza según EXIF. */
private fun cargarFoto(ruta: String, maxLado: Int): Bitmap? {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    var muestra = 1
    while (medidas.outWidth / muestra > maxLado || medidas.outHeight / muestra > maxLado) muestra *= 2
    val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestra })
        ?: return null

    val grados = when (
        ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }
    val matriz = Matrix().apply { postRotate(grados) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
}
