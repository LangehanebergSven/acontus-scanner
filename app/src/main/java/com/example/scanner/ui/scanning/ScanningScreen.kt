package com.example.scanner.ui.scanning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.model.Warehouse

@Composable
fun ScanningScreen(
    rootNavController: NavController,
    viewModel: ScanningViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    // Reload data when the screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.loadActiveProcess()
    }

    Scaffold(
        floatingActionButton = {
            if (uiState is ScanningUiState.Success) {
                Box {
                    FloatingActionButton(
                        onClick = { isFabMenuExpanded = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Aktionen")
                    }
                    DropdownMenu(
                        expanded = isFabMenuExpanded,
                        onDismissRequest = { isFabMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Konfiguration bearbeiten") },
                            onClick = {
                                rootNavController.navigate("edit_configuration")
                                isFabMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Konfiguration bearbeiten"
                                )
                            })
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Senden") },
                            onClick = {
                                viewModel.submitProcess()
                                isFabMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Senden"
                                )
                            })
                        DropdownMenuItem(
                            text = { Text("Abbrechen", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                viewModel.cancelProcess()
                                isFabMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = "Abbrechen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            })
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ScanningUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ScanningUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ScanningUiState.NoProcess -> {
                    NoProcessContent(
                        onStartProcess = { rootNavController.navigate("process_configuration") }
                    )
                }
                is ScanningUiState.Success -> {
                    val activeWarehouse by viewModel.activeWarehouse.collectAsState()
                    val activeBookingReason by viewModel.activeBookingReason.collectAsState()
                    val activeBatchNumber by viewModel.activeBatchNumber.collectAsState()
                    val activeBestBeforeDate by viewModel.activeBestBeforeDate.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CompactConfigurationHeader(
                            warehouse = activeWarehouse,
                            bookingReason = activeBookingReason,
                            batchNumber = activeBatchNumber,
                            bestBeforeDate = activeBestBeforeDate?.toString() // Simplified
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.scannedItems.isEmpty()) {
                            EmptyState()
                        } else {
                            ScannedItemsList(items = state.scannedItems)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoProcessContent(onStartProcess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kein aktiver Scan-Prozess", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartProcess) {
            Text("Neuen Prozess starten")
        }
    }
}

@Composable
private fun CompactConfigurationHeader(
    warehouse: Warehouse?,
    bookingReason: BookingReason?,
    batchNumber: String?,
    bestBeforeDate: String?
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConfigItem(label = "Lager:", value = warehouse?.name ?: "-")
                ConfigItem(label = "Grund:", value = bookingReason?.reason ?: "-")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ConfigItem(label = "Charge:", value = batchNumber ?: "-")
                ConfigItem(label = "MHD:", value = bestBeforeDate ?: "-")
            }
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Noch keine Artikel gescannt.",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ScannedItemsList(items: List<ScannedItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Text(text = "Scanned Item ID: ${item.id} | Qty: ${item.quantity}")
        }
    }
}
