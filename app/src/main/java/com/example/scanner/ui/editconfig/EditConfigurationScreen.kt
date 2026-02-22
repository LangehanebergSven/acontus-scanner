package com.example.scanner.ui.editconfig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Warehouse
import com.example.scanner.ui.scanning.ScanningUiState
import com.example.scanner.ui.scanning.ScanningViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConfigurationScreen(
    navController: NavController,
    viewModel: ScanningViewModel // Use the shared ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfiguration bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ScanningUiState.Success -> {
                    EditConfigurationContent(state = state, viewModel = viewModel)
                }
                else -> {
                    // Show a loading or error state if needed
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditConfigurationContent(
    state: ScanningUiState.Success,
    viewModel: ScanningViewModel
) {
    val activeWarehouse by viewModel.activeWarehouse.collectAsState()
    val activeBookingReason by viewModel.activeBookingReason.collectAsState()
    val activeBatchNumber by viewModel.activeBatchNumber.collectAsState()
    val activeBestBeforeDate by viewModel.activeBestBeforeDate.collectAsState()

    var showWarehouseDialog by remember { mutableStateOf(false) }
    var showBookingReasonDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = {
                viewModel.setActiveBestBeforeDate(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val readOnlyColors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Warehouse Selector
        Box(modifier = Modifier.clickable { showWarehouseDialog = true }) {
            OutlinedTextField(
                value = activeWarehouse?.name ?: "-",
                onValueChange = {},
                readOnly = true,
                label = { Text("Aktives Lager") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = readOnlyColors
            )
        }

        // Booking Reason Selector
        Box(modifier = Modifier.clickable { showBookingReasonDialog = true }) {
            OutlinedTextField(
                value = activeBookingReason?.reason ?: "-",
                onValueChange = {},
                readOnly = true,
                label = { Text("Aktiver Buchungsgrund") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = readOnlyColors
            )
        }
        
        // MHD and Batch Number Inputs
        val mhdFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
        Box(modifier = Modifier.clickable { showDatePicker = true }) {
            OutlinedTextField(
                value = activeBestBeforeDate?.let { mhdFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("MHD") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = readOnlyColors
            )
        }
        OutlinedTextField(
            value = activeBatchNumber ?: "",
            onValueChange = { viewModel.setActiveBatchNumber(it) },
            label = { Text("Charge") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(state = datePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Date(it + TimeZone.getDefault().getOffset(it))
                            onDateSelected(selectedDate)
                        }
                    }) {
                        Text("OK")
                    }
                }
            }
        }
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
