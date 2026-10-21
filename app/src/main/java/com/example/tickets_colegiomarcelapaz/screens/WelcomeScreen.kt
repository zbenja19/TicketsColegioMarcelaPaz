package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF10233D)
private val Accent = Color(0xFFF5B83D)
private val Cream = Color(0xFFF4F1EA)
private val SoftBlue = Color(0xFFC9D6E8)
private val Border = Color(0xFF4A6285)

/**
 * Portada de la app.
 *
 * @param onRegister navega a la pantalla de registro (crear cuenta).
 * @param onLogin navega a la pantalla de inicio de sesión.
 */
@Composable
fun WelcomeScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "COLEGIO MARCELA PAZ",
            color = SoftBlue,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 1.2.sp,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TicketIllustration(Modifier.size(width = 320.dp, height = 270.dp))
        }

        Text(
            text = buildAnnotatedString {
                append("Reporta, ")
                withStyle(SpanStyle(color = Accent)) { append("repara") }
                append(".")
            },
            color = Color.White,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
        )

        Text(
            text = "Crea un reporte para dar aviso sobre algún objeto o lugar del colegio " +
                    "en mal estado y que necesite atención. Podrás ver el seguimiento " +
                    "de tus reportes en tiempo real.",
            color = SoftBlue,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )

        Column(
            modifier = Modifier.padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Acción principal: registro
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Navy,
                ),
            ) {
                Text("Crear cuenta", style = MaterialTheme.typography.labelLarge, fontSize = 17.sp)
            }

            // Acción secundaria: inicio de sesión
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = BorderStroke(1.5.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge, fontSize = 17.sp)
            }
        }
    }
}

/** Ticket dibujado con Canvas. Se diseña en un lienzo de 320x270 y se escala. */
@Composable
private fun TicketIllustration(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val s = size.width / 320f
        val toSp = { v: Float -> (v / (density * fontScale)).sp }
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            rotate(degrees = -5f, pivot = Offset(160f, 135f)) {

                // Cuerpo del ticket
                drawRoundRect(
                    color = Cream,
                    topLeft = Offset(12f, 20f),
                    size = Size(296f, 230f),
                    cornerRadius = CornerRadius(12f),
                )
                // Muescas laterales
                drawCircle(Navy, radius = 16f, center = Offset(12f, 110f))
                drawCircle(Navy, radius = 16f, center = Offset(308f, 110f))
                // Línea punteada
                drawLine(
                    color = Navy.copy(alpha = 0.35f),
                    start = Offset(32f, 110f),
                    end = Offset(288f, 110f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 7f)),
                )

                // Etiqueta "Abierto"
                drawRoundRect(
                    color = Accent,
                    topLeft = Offset(36f, 42f),
                    size = Size(84f, 26f),
                    cornerRadius = CornerRadius(13f),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Abierto",
                    topLeft = Offset(53f, 47f),
                    style = TextStyle(color = Navy, fontSize = toSp(13f), fontWeight = FontWeight.Bold),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "#0124",
                    topLeft = Offset(246f, 46f),
                    style = TextStyle(color = Navy, fontSize = toSp(14f), fontWeight = FontWeight.Bold),
                )

                // Línea de título
                drawRoundRect(
                    color = Navy,
                    topLeft = Offset(36f, 80f),
                    size = Size(150f, 10f),
                    cornerRadius = CornerRadius(5f),
                )

                // Llave inglesa
                rotate(degrees = 45f, pivot = Offset(160f, 178f)) {
                    drawLine(
                        color = Navy,
                        start = Offset(104f, 178f),
                        end = Offset(194f, 178f),
                        strokeWidth = 12f,
                        cap = StrokeCap.Round,
                    )
                    drawArc(
                        color = Navy,
                        startAngle = 40f,
                        sweepAngle = 280f,
                        useCenter = false,
                        topLeft = Offset(172f, 156f),
                        size = Size(44f, 44f),
                        style = Stroke(width = 10f, cap = StrokeCap.Round),
                    )
                }

                // Líneas de detalle
                drawRoundRect(
                    color = Navy.copy(alpha = 0.35f),
                    topLeft = Offset(36f, 212f),
                    size = Size(120f, 8f),
                    cornerRadius = CornerRadius(4f),
                )
                drawRoundRect(
                    color = Navy.copy(alpha = 0.2f),
                    topLeft = Offset(36f, 228f),
                    size = Size(80f, 8f),
                    cornerRadius = CornerRadius(4f),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onRegister = {}, onLogin = {})
    }
}