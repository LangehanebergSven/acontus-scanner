package com.example.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scanner.ui.editconfig.EditConfigurationScreen
import com.example.scanner.ui.main.MainScreen
import com.example.scanner.ui.process.ProcessConfigurationScreen
import com.example.scanner.ui.scanning.ScanningViewModel
import com.example.scanner.ui.theme.ScannerTheme
import com.example.scanner.ui.view.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScannerTheme {
                ScannerApp()
            }
        }
    }
}

@Composable
fun ScannerApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main/0") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable(
            "main/{processId}",
            arguments = listOf(navArgument("processId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val processId = backStackEntry.arguments?.getLong("processId") ?: 0L
            val scanningViewModel: ScanningViewModel = hiltViewModel(backStackEntry)
            MainScreen(
                processId = processId,
                rootNavController = navController,
                scanningViewModel = scanningViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main/$processId") { inclusive = true }
                    }
                }
            )
        }
        composable("process_configuration") {
            ProcessConfigurationScreen(
                viewModel = hiltViewModel(),
                onNavigateToScanning = { processId ->
                    navController.navigate("main/$processId") {
                        popUpTo("login")
                    }
                }
            )
        }
        composable("edit_configuration") {
            // Get the ViewModel from the previous screen, which is main/{processId}
            // This is safer than hardcoding a route.
            val mainBackStackEntry = remember(it) { navController.previousBackStackEntry }
            if (mainBackStackEntry != null) {
                val scanningViewModel: ScanningViewModel = hiltViewModel(mainBackStackEntry)
                EditConfigurationScreen(
                    navController = navController,
                    viewModel = scanningViewModel
                )
            }
        }
    }
}
