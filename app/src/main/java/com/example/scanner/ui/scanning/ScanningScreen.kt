package com.example.scanner.ui.scanning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.model.Warehouse
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ScanningScreen(
    viewModel: ScanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val state = uiState) {
                is ScanningUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ScanningUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ScanningUiState.Success -> {
                    ScanningContent(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanningContent(
    state: ScanningUiState.Success,
    viewModel: ScanningViewModel
) {
    val activeWarehouse by viewModel.activeWarehouse.collectAsState()
    val activeBookingReason by viewModel.activeBookingReason.collectAsState()
    val activeBatchNumber by viewModel.activeBatchNumber.collectAsState()

    // Dialog states
    var showWarehouseDialog by remember { mutableStateOf(false) }
    var showBookingReasonDialog by remember { mutableStateOf(false) }

    if (showWarehouseDialog) {
        SelectionDialog(
            title = "Lager wählen",
            items = state.allWarehouses,
            itemText = { it.name },
            onDismiss = { showWarehouseDialog = false },
            onSelect = {
                viewModel.setActiveWarehouse(it)
                showWarehouseDialog = false
            }
        )
    }

    if (showBookingReasonDialog) {
        SelectionDialog(
            title = "Buchungsgrund wählen",
            items = state.allBookingReasons,
            itemText = { it.reason },
            onDismiss = { showBookingReasonDialog = false },
            onSelect = {
                viewModel.setActiveBookingReason(it)
                showBookingReasonDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ConfigurationHeader(
            warehouse = activeWarehouse,
            bookingReason = activeBookingReason,
            onWarehouseClick = { showWarehouseDialog = true },
            onBookingReasonClick = { showBookingReasonDialog = true }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // MHD and Batch Number Inputs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = "DD.MM.YYYY", // Placeholder for DatePicker
                onValueChange = { /* TODO: Implement DatePicker */ },
                label = { Text("MHD") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = activeBatchNumber ?: "",
                onValueChange = { viewModel.setActiveBatchNumber(it) },
                label = { Text("Charge") },
                modifier = Modifier.weight(1f)
            )
        }

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

@Composable
private fun ConfigurationHeader(
    warehouse: Warehouse?,
    bookingReason: BookingReason?,
    onWarehouseClick: () -> Unit,
    onBookingReasonClick: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onWarehouseClick)
            ) {
                Text(text = "Akt. Lager", style = MaterialTheme.typography.labelMedium)
                Text(text = warehouse?.name ?: "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onBookingReasonClick)
            ) {
                Text(text = "Akt. Buchungsgrund", style = MaterialTheme.typography.labelMedium)
                Text(text = bookingReason?.reason ?: "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
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
private fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    itemText: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(items) { item ->
                        Text(
                            text = itemText(item),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannedItemsList(items: List<ScannedItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            // TODO: Replace with a proper item layout in Task 4.3
            Text(text = "Scanned Item ID: ${item.id} | Qty: ${item.quantity}")
        }
    }
}
