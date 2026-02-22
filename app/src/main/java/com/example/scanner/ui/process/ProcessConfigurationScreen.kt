package com.example.scanner.ui.process

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Warehouse

@Composable
fun ProcessConfigurationScreen(
    employeeId: String,
    onNavigateToScanning: (Long) -> Unit,
    viewModel: ProcessConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is ProcessUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProcessUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProcessUiState.Success -> {
                ProcessConfigurationContent(
                    warehouses = state.warehouses,
                    bookingReasons = state.bookingReasons,
                    onStartClicked = {
                        viewModel.startProcess(employeeId, onNavigateToScanning)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ProcessConfigurationContent(
    warehouses: List<Warehouse>,
    bookingReasons: List<BookingReason>,
    onStartClicked: () -> Unit,
    viewModel: ProcessConfigurationViewModel
) {
    val selectedWarehouse by viewModel.selectedWarehouse.collectAsState()
    val selectedBookingReason by viewModel.selectedBookingReason.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Prozess konfigurieren", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Warehouse Dropdown
        DropdownSelector(
            label = "Lager",
            items = warehouses,
            selectedItem = selectedWarehouse,
            onItemSelected = { viewModel.selectWarehouse(it) },
            itemToString = { it.name }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Booking Reason Dropdown
        DropdownSelector(
            label = "Buchungsgrund",
            items = bookingReasons,
            selectedItem = selectedBookingReason,
            onItemSelected = { viewModel.selectBookingReason(it) },
            itemToString = { it.reason }
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartClicked,
            enabled = selectedWarehouse != null && selectedBookingReason != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PROZESS STARTEN")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemToString) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
