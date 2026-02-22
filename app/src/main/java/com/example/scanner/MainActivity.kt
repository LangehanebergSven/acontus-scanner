package com.example.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
                // Navigate to the nested main_graph and clear the back stack
                navController.navigate("main_graph/0") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        // Nest the main and edit configuration screens in a shared graph
        mainGraph(navController)

        composable("process_configuration") {
            ProcessConfigurationScreen(
                viewModel = hiltViewModel(),
                onNavigateToScanning = { processId ->
                    // Navigate to the main_graph with the new processId
                    navController.navigate("main_graph/$processId") {
                        // Pop up to the login screen to prevent going back to an empty main screen
                        popUpTo("login")
                    }
                }
            )
        }
    }
}

// Define the nested graph as an extension function
private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = "main/{processId}",
        route = "main_graph/{processId}",
        arguments = listOf(navArgument("processId") { type = NavType.LongType; defaultValue = 0L })
    ) {
        composable("main/{processId}") { backStackEntry ->
            // Get the parent entry for the graph to scope the ViewModel
            val parentEntry = backStackEntry.destination.parent?.route?.let {
                navController.getBackStackEntry(it)
            }
            val scanningViewModel: ScanningViewModel = hiltViewModel(parentEntry!!)

            val processId = backStackEntry.arguments?.getLong("processId") ?: 0L
            MainScreen(
                processId = processId,
                rootNavController = navController,
                scanningViewModel = scanningViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main_graph/{processId}") { inclusive = true }
                    }
                }
            )
        }
        composable("edit_configuration") { backStackEntry ->
            // Get the parent entry for the graph to scope the ViewModel
            val parentEntry = backStackEntry.destination.parent?.route?.let {
                navController.getBackStackEntry(it)
            }
            val scanningViewModel: ScanningViewModel = hiltViewModel(parentEntry!!)
            EditConfigurationScreen(
                navController = navController,
                viewModel = scanningViewModel
            )
        }
    }
}
