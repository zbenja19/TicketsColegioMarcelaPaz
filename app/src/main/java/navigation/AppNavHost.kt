package com.example.tickets_colegiomarcelapaz.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tickets_colegiomarcelapaz.screens.HomeScreen
import com.example.tickets_colegiomarcelapaz.screens.LoginScreen
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
        if (authViewModel.state.value.usuario != null) Routes.HOME else Routes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.LOGIN) {
            LaunchedEffect(Unit) { authViewModel.limpiarError() }
            LoginScreen(
                onLogin = { email, password ->
                    authViewModel.login(email, password) { navController.irAHomeLimpio() }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) { popUpTo(Routes.WELCOME) }
                },
                onBack = { navController.popBackStack() },
                errorMessage = state.error,
            )
        }
        composable(Routes.REGISTER) {
            LaunchedEffect(Unit) { authViewModel.limpiarError() }
            RegisterScreen(
                onRegister = { name, email, password ->
                    authViewModel.registrar(name, email, password) {
                        navController.irAHomeLimpio()
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) }
                },
                onBack = { navController.popBackStack() },
                errorMessage = state.error,
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                nombre = state.usuario?.nombre ?: "",
                onLogout = {
                    authViewModel.cerrarSesion {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}

/** Va a Inicio y vacía la pila, para que "atrás" no regrese al login. */
private fun NavHostController.irAHomeLimpio() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}