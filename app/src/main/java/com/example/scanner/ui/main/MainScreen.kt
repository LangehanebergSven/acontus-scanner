package com.example.scanner.ui.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scanner.ui.home.HomeScreen
import com.example.scanner.ui.scanning.ScanningScreen
import com.example.scanner.ui.scanning.ScanningViewModel
import com.example.scanner.ui.view.settings.SettingsScreen

@Composable
fun MainScreen(
    processId: Long,
    rootNavController: NavController,
    scanningViewModel: ScanningViewModel,
    mainViewModel: MainViewModel,
    onLogout: () -> Unit,
    onStartNewProcess: () -> Unit
) {
    LaunchedEffect(processId) {
        scanningViewModel.loadActiveProcess(processId)
    }

    val employeeName by mainViewModel.employeeName.collectAsState()
    val navController = rememberNavController()
    val items = listOf(
        BottomNavigationItem.Home,
        BottomNavigationItem.Scanning,
        BottomNavigationItem.Settings,
    )

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.height(64.dp)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavigationItem.Scanning.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavigationItem.Home.route) {
                HomeScreen(
                    navController = navController,
                    userName = employeeName ?: ""
                )
            }
            composable(BottomNavigationItem.Scanning.route) {
                ScanningScreen(
                    rootNavController = rootNavController,
                    viewModel = scanningViewModel,
                    onStartNewProcess = onStartNewProcess
                )
            }
            composable(BottomNavigationItem.Settings.route) {
                SettingsScreen(onLogout = onLogout, onStartProcess = onStartNewProcess)
            }
        }
    }
}
