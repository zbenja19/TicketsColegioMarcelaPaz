package com.example.tickets_colegiomarcelapaz.validation

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun validateName(value: String): String? = when {
    value.isBlank() -> "Ingresa tu nombre"
    value.trim().length < 3 -> "Debe tener al menos 3 caracteres"
    else -> null
}

fun validateEmail(value: String): String? = when {
    value.isBlank() -> "Ingresa tu correo"
    !EMAIL_REGEX.matches(value.trim()) -> "Correo no válido"
    else -> null
}

fun validatePassword(value: String, strict: Boolean): String? = when {
    value.isBlank() -> "Ingresa tu contraseña"
    strict && value.length < 8 -> "Mínimo 8 caracteres"
    strict && (value.none { it.isLetter() } || value.none { it.isDigit() }) ->
        "Debe incluir letras y números"
    else -> null
}

fun validateConfirmPassword(password: String, confirm: String): String? = when {
    confirm.isBlank() -> "Confirma tu contraseña"
    password != confirm -> "Las contraseñas no coinciden"
    else -> null
}

val SECTORES = listOf("Sala 12", "Biblioteca", "Gimnasio", "Baños", "Patio", "Laboratorio", "Otro")
val URGENCIAS = listOf("Baja", "Media", "Alta")

fun validateTitle(value: String): String? = when {
    value.isBlank() -> "Ingresa un título"
    value.trim().length < 5 -> "Mínimo 5 caracteres"
    else -> null
}

fun validateDescription(value: String): String? = when {
    value.isBlank() -> "Describe el problema"
    value.trim().length < 10 -> "Mínimo 10 caracteres"
    else -> null
}

fun validateSector(value: String): String? =
    if (value !in SECTORES) "Elige un sector" else null

fun validatePlace(sector: String, place: String): String? =
    if (sector == "Otro" && place.isBlank()) "Indica el lugar exacto" else null

fun validateUrgency(value: String): String? =
    if (value !in URGENCIAS) "Elige el nivel de urgencia" else null

val TIPOS_DOCUMENTO = arrayOf(
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
)
const val MAX_DOCUMENTO_BYTES = 10L * 1024 * 1024

fun validateDocument(type: String, size: Long): String? = when {
    type !in TIPOS_DOCUMENTO -> "Solo se permiten archivos PDF o Word"
    size > MAX_DOCUMENTO_BYTES -> "El archivo supera los 10 MB"
    else -> null
}
