package com.example.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scanner.ui.process.ProcessConfigurationScreen
import com.example.scanner.ui.scanning.ScanningScreen
import com.example.scanner.ui.theme.ScannerTheme
import com.example.scanner.ui.view.login.LoginScreen
import com.example.scanner.ui.view.settings.SettingsScreen
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
                navController.navigate("settings") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("settings") {
            SettingsScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                },
                onStartProcess = {
                    navController.navigate("process_configuration")
                }
            )
        }
        composable("process_configuration") {
            ProcessConfigurationScreen(
                onNavigateToScanning = { processId ->
                    navController.navigate("scanning/$processId")
                }
            )
        }
        composable(
            route = "scanning/{processId}",
            arguments = listOf(navArgument("processId") { type = NavType.LongType })
        ) {
            ScanningScreen()
        }
    }
}
