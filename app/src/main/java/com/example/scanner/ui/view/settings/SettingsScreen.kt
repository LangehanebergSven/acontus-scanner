package com.example.scanner.ui.view.settings

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.R
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
                title = { 
                    Text(
                        "Einstellungen",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.onLogoutClicked()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abmelden")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InfoCard()
                Spacer(modifier = Modifier.height(32.dp))
                MasterDataCard(
                    lastSyncTimestamp = lastSyncTimestamp,
                    isLoading = isLoading,
                    onUpdateMasterData = { viewModel.onClearCacheClicked() }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Powered by",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.acontus_rgb),
                    contentDescription = "Acontus Logo",
                    modifier = Modifier.height(30.dp)
                )
            }
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingItem(
                icon = Icons.Default.Info,
                title = "Build-Nummer",
                value = "20240729-01 (Dummy)"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingItem(
                icon = Icons.Default.Info,
                title = "Unternehmen",
                value = "Musterfirma GmbH"
            )
        }
    }
}

@Composable
private fun MasterDataCard(lastSyncTimestamp: Long, isLoading: Boolean, onUpdateMasterData: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Stammdaten",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stammdaten umfassen Artikel, Lager und Mitarbeiter. Diese werden lokal gespeichert, um das Arbeiten ohne Internetverbindung zu ermöglichen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val formattedDate = if (lastSyncTimestamp > 0) {
                val date = Date(lastSyncTimestamp)
                SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY).format(date)
            } else {
                "Nie"
            }
            SettingItem(
                icon = Icons.Default.Storage,
                title = "Letzte Aktualisierung",
                value = formattedDate
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUpdateMasterData,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Stammdaten jetzt aktualisieren")
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
