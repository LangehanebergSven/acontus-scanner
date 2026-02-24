package com.example.scanner.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.ui.theme.ScannerTheme
import com.example.scanner.ui.viewmodel.SettingsUiEvent
import com.example.scanner.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    // This is no longer used, but kept to avoid breaking the NavHost for now.
    // It will be removed in a later step.
    onStartProcess: () -> Unit
) {
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is SettingsUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                actions = {
                    IconButton(onClick = {
                        viewModel.onLogoutClicked()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Abmelden")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoCard()
            Spacer(modifier = Modifier.height(32.dp))
            CacheCard(
                lastSyncTimestamp = lastSyncTimestamp,
                isLoading = isLoading,
                onClearCache = { viewModel.onClearCacheClicked() }
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingItem(
                icon = Icons.Default.Info,
                title = "App Version",
                value = "1.0.0 (Dummy)"
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingItem(
                icon = Icons.Default.Info,
                title = "Build-Nummer",
                value = "20240729-01 (Dummy)"
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingItem(
                icon = Icons.Default.Info,
                title = "Unternehmen",
                value = "Musterfirma GmbH"
            )
        }
    }
}

@Composable
private fun CacheCard(lastSyncTimestamp: Long, isLoading: Boolean, onClearCache: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedDate = if (lastSyncTimestamp > 0) {
                val date = Date(lastSyncTimestamp)
                SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY).format(date)
            } else {
                "Nie"
            }
            SettingItem(
                icon = Icons.Default.Storage,
                title = "Letzte Cache-Aktualisierung",
                value = formattedDate
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClearCache,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Cache jetzt leeren")
                }
            }
        }
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
