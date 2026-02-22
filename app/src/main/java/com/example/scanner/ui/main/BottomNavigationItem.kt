package com.example.scanner.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavigationItem("home", Icons.Default.Home, "Home")
    object Scanning : BottomNavigationItem("scanning", Icons.Default.QrCodeScanner, "Scannen")
    object Settings : BottomNavigationItem("settings", Icons.Default.Settings, "Einstellungen")
}
