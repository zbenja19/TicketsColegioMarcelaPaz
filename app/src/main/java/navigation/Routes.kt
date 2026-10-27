package com.example.tickets_colegiomarcelapaz.navigation

object Routes {
    // Grafo exterior (AppNavHost)
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val NUEVO_TICKET = "nuevo_ticket"
    const val PDF = "pdf/{id}"
    fun pdf(id: Long) = "pdf/$id"

    // Grafo interior: pestañas de MainScreen
    const val HOME = "home"
    const val DOCUMENTOS = "documentos"
    const val PERFIL = "perfil"
}
