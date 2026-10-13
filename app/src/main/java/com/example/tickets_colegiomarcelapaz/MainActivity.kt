package com.example.tickets_colegiomarcelapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tickets_colegiomarcelapaz.screens.WelcomeScreen
import com.example.tickets_colegiomarcelapaz.ui.theme.TicketsColegioMarcelaPazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketsColegioMarcelaPazTheme {
                WelcomeScreen(
                    onCreateReport = {},
                    onLogin = {}
                )
            }
        }
    }
}