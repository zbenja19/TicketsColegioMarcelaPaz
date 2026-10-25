package com.example.tickets_colegiomarcelapaz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tickets_colegiomarcelapaz.screens.LoginScreen
import com.example.tickets_colegiomarcelapaz.screens.RegisterScreen
import com.example.tickets_colegiomarcelapaz.screens.WelcomeScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
        modifier = modifier,
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { email, password -> /* TODO Fase 1: autenticar */ },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.WELCOME)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegister = { name, email, password -> /* TODO Fase 1: guardar usuario */ },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}