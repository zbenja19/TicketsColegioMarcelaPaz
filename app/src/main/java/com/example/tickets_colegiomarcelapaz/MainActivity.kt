package com.example.tickets_colegiomarcelapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tickets_colegiomarcelapaz.screens.LoginScreen
import com.example.tickets_colegiomarcelapaz.screens.RegisterScreen
import com.example.tickets_colegiomarcelapaz.screens.WelcomeScreen
import com.example.tickets_colegiomarcelapaz.ui.theme.TicketsColegioMarcelaPazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketsColegioMarcelaPazTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            onRegister = { navController.navigate("register") },
                            onLogin = { navController.navigate("login") },
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLogin = { email, password -> /* TODO: autenticar */ },
                            onNavigateToRegister = { navController.navigate("register") },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegister = { name, email, password -> /* TODO: guardar usuario */ },
                            onNavigateToLogin = { navController.navigate("login") },
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}