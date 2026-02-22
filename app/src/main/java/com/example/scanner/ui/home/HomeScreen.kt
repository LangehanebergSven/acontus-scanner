package com.example.scanner.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.scanner.ui.main.BottomNavigationItem

@Composable
fun HomeScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Willkommen!",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate(BottomNavigationItem.Scanning.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scannen starten / fortsetzen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(BottomNavigationItem.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Einstellungen")
            }
        }
    }
}
