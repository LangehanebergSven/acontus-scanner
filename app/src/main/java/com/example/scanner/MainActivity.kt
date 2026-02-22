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
import com.example.scanner.ui.main.MainViewModel
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
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { employeeId ->
                mainViewModel.findLatestProcess(employeeId) { processId ->
                    navController.navigate("main_graph/$processId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            })
        }

        mainGraph(navController, mainViewModel)

        composable("process_configuration") {
            // Make sure we have an employeeId before navigating here.
            // If not, redirect to login. This is a safeguard.
            val employeeId = mainViewModel.loggedInEmployeeId
            if (employeeId == null) {
                navController.navigate("login") {
                    popUpTo("main_graph") { inclusive = true }
                }
            } else {
                ProcessConfigurationScreen(
                    employeeId = employeeId,
                    viewModel = hiltViewModel(),
                    onNavigateToScanning = { processId ->
                        navController.navigate("main_graph/$processId") {
                            popUpTo("login")
                        }
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavHostController, mainViewModel: MainViewModel) {
    navigation(
        startDestination = "main/{processId}",
        route = "main_graph/{processId}",
        arguments = listOf(navArgument("processId") { type = NavType.LongType; defaultValue = 0L })
    ) {
        composable("main/{processId}") { backStackEntry ->
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
                },
                onStartNewProcess = {
                    // Navigate to process configuration
                    navController.navigate("process_configuration")
                }
            )
        }
        composable("edit_configuration") { backStackEntry ->
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
