package com.example.tickets_colegiomarcelapaz.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tickets_colegiomarcelapaz.navigation.Routes
import kotlinx.coroutines.launch

private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana(Routes.HOME, "Inicio", Icons.Filled.Home),
    Pestana(Routes.DOCUMENTOS, "Documentos", Icons.AutoMirrored.Filled.List),
    Pestana(Routes.PERFIL, "Perfil", Icons.Filled.Person),
)

/**
 * Estructura principal tras iniciar sesión: barra superior, barra inferior y menú lateral.
 * Tiene su propio NavHost (interno) para las pestañas, así las barras no se redibujan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    nombre: String,
    email: String,
    onNuevoTicket: () -> Unit,
    onAbrirPdf: (Long) -> Unit,
    onLogout: () -> Unit,
) {
    val tabsNav = rememberNavController()
    val backStack by tabsNav.currentBackStackEntryAsState()
    val rutaActual = backStack?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // Cambia de pestaña sin apilar pantallas y recordando el scroll de cada una.
    fun irA(ruta: String) {
        tabsNav.navigate(ruta) {
            popUpTo(tabsNav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Colegio Marcela Paz",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp),
                )
                PESTANAS.forEach { p ->
                    NavigationDrawerItem(
                        label = { Text(p.titulo) },
                        icon = { Icon(p.icono, contentDescription = null) },
                        selected = rutaActual == p.ruta,
                        onClick = {
                            irA(p.ruta)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        confirmarSalida = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(PESTANAS.firstOrNull { it.ruta == rutaActual }?.titulo ?: "") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    PESTANAS.forEach { p ->
                        NavigationBarItem(
                            selected = rutaActual == p.ruta,
                            onClick = { irA(p.ruta) },
                            icon = { Icon(p.icono, contentDescription = null) },
                            label = { Text(p.titulo) },
                            // El amarillo (secondary) casi no se lee sobre fondo claro.
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = tabsNav,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(tween(250)) },
                exitTransition = { fadeOut(tween(250)) },
            ) {
                composable(Routes.HOME) {
                    HomeScreen(nombre = nombre, onNuevoTicket = onNuevoTicket)
                }
                composable(Routes.DOCUMENTOS) {
                    DocumentosScreen(onAbrirPdf = onAbrirPdf)
                }
                composable(Routes.PERFIL) {
                    PerfilScreen(nombre = nombre, email = email, onLogout = { confirmarSalida = true })
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar tu sesión?") },
            confirmButton = {
                TextButton(onClick = { confirmarSalida = false; onLogout() }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            },
        )
    }
}
