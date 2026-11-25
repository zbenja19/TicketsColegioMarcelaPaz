package com.example.tickets_colegiomarcelapaz.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tickets_colegiomarcelapaz.screens.LoginScreen
import com.example.tickets_colegiomarcelapaz.screens.MainScreen
import com.example.tickets_colegiomarcelapaz.screens.NuevoTicketScreen
import com.example.tickets_colegiomarcelapaz.screens.PdfViewerScreen
import com.example.tickets_colegiomarcelapaz.screens.RegisterScreen
import com.example.tickets_colegiomarcelapaz.screens.WelcomeScreen
import com.example.tickets_colegiomarcelapaz.ui.theme.BrandNavy
import com.example.tickets_colegiomarcelapaz.viewmodel.AuthViewModel

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val state by authViewModel.state.collectAsStateWithLifecycle()

    if (!state.sesionVerificada) {
        // Mientras se lee la sesión guardada, mostramos el fondo de la portada.
        Box(modifier.fillMaxSize().background(BrandNavy))
    } else {
        AppNavGraph(authViewModel, modifier)
    }
}

@Composable
private fun AppNavGraph(authViewModel: AuthViewModel, modifier: Modifier) {
    val navController = rememberNavController()
    val state by authViewModel.state.collectAsStateWithLifecycle()

    // Se calcula una sola vez: cambiarlo después reconstruiría el grafo.
    val startDestination = remember {
        if (authViewModel.state.value.usuario != null) Routes.MAIN else Routes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Animación al cambiar de pantalla: la nueva entra deslizándose desde la derecha.
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() },
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.LOGIN) {
            LaunchedEffect(Unit) { authViewModel.limpiarError() }
            val form by authViewModel.loginForm.collectAsStateWithLifecycle()
            LoginScreen(
                form = form,
                cargando = state.cargando,
                errorMessage = state.error,
                onEmailChange = authViewModel::onLoginEmail,
                onPasswordChange = authViewModel::onLoginPassword,
                onSubmit = { authViewModel.login { navController.irAMainLimpio() } },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) { popUpTo(Routes.WELCOME) }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.REGISTER) {
            LaunchedEffect(Unit) { authViewModel.limpiarError() }
            val form by authViewModel.registerForm.collectAsStateWithLifecycle()
            RegisterScreen(
                form = form,
                cargando = state.cargando,
                errorMessage = state.error,
                onNombreChange = authViewModel::onRegisterNombre,
                onEmailChange = authViewModel::onRegisterEmail,
                onPasswordChange = authViewModel::onRegisterPassword,
                onConfirmarChange = authViewModel::onRegisterConfirmar,
                onSubmit = { authViewModel.registrar { navController.irAMainLimpio() } },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.MAIN) {
            MainScreen(
                nombre = state.usuario?.nombre ?: "",
                email = state.usuario?.email ?: "",
                onNuevoTicket = { navController.navigate(Routes.NUEVO_TICKET) },
                onAbrirPdf = { id -> navController.navigate(Routes.pdf(id)) },
                onLogout = {
                    authViewModel.cerrarSesion {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.NUEVO_TICKET) {
            NuevoTicketScreen(
                onBack = { navController.popBackStack() },
                onGuardado = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.PDF,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            PdfViewerScreen(
                documentoId = entry.arguments?.getLong("id") ?: 0L,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** Va a la pantalla principal y vacía la pila, para que "atrás" no regrese al login. */
private fun NavHostController.irAMainLimpio() {
    navigate(Routes.MAIN) {
        popUpTo(graph.id) { inclusive = true }
    }
}
