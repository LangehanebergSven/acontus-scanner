package com.example.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("main") {
            // The ViewModel is created here and scoped to the "main" navigation entry.
            // It's then passed down to MainScreen, which can pass it to ScanningScreen.
            val scanningViewModel: ScanningViewModel = hiltViewModel(
                navController.getBackStackEntry("main")
            )
            MainScreen(
                rootNavController = navController,
                scanningViewModel = scanningViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("process_configuration") {
            // This screen gets its own ViewModel instance.
            ProcessConfigurationScreen(
                viewModel = hiltViewModel(),
                onNavigateToScanning = {
                    navController.navigate("main") {
                        popUpTo("process_configuration") { inclusive = true }
                    }
                }
            )
        }
        composable("edit_configuration") {
            // This gets the SAME ViewModel instance as MainScreen because it's scoped to "main".
            val scanningViewModel: ScanningViewModel = hiltViewModel(
                navController.getBackStackEntry("main")
            )
            EditConfigurationScreen(
                navController = navController,
                viewModel = scanningViewModel
            )
        }
    }
}
