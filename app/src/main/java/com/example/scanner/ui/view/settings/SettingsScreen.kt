package com.example.scanner.ui.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.ui.theme.ScannerTheme
import com.example.scanner.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onStartProcess: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onStartProcess) {
                Text("Neuen Prozess starten")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { viewModel.onClearCacheClicked() }) {
                Text("Cache leeren")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.onSyncClicked() }) {
                Text("Offline-Daten synchronisieren")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                viewModel.onLogoutClicked()
                onLogout()
            }) {
                Text("Abmelden")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ScannerTheme {
        SettingsScreen(onLogout = {}, onStartProcess = {})
    }
}
