package com.example.scanner.ui.editconfig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var showSaveChangesDialog by remember { mutableStateOf(false) }

    val activeWarehouse by viewModel.activeWarehouse.collectAsState()
    val activeBookingReason by viewModel.activeBookingReason.collectAsState()
    val activeBatchNumber by viewModel.activeBatchNumber.collectAsState()
    val activeBestBeforeDate by viewModel.activeBestBeforeDate.collectAsState()

    // Temporary states for editing
    var tempWarehouse by remember(activeWarehouse) { mutableStateOf(activeWarehouse) }
    var tempBookingReason by remember(activeBookingReason) { mutableStateOf(activeBookingReason) }
    var tempBatchNumber by remember(activeBatchNumber) { mutableStateOf(activeBatchNumber) }
    var tempBestBeforeDate by remember(activeBestBeforeDate) { mutableStateOf(activeBestBeforeDate) }

    val hasChanges = tempWarehouse != activeWarehouse ||
            tempBookingReason != activeBookingReason ||
            tempBatchNumber != activeBatchNumber ||
            tempBestBeforeDate != activeBestBeforeDate

    if (showSaveChangesDialog) {
        SaveChangesDialog(
            onConfirm = {
                viewModel.setActiveWarehouse(tempWarehouse)
                viewModel.setActiveBookingReason(tempBookingReason)
                viewModel.setActiveBatchNumber(tempBatchNumber ?: "")
                viewModel.setActiveBestBeforeDate(tempBestBeforeDate)
                showSaveChangesDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showSaveChangesDialog = false
                navController.popBackStack()
            },
            onCancel = {
                showSaveChangesDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfiguration bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showSaveChangesDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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
                    EditConfigurationContent(
                        state = state,
                        tempWarehouse = tempWarehouse,
                        onTempWarehouseChange = { tempWarehouse = it },
                        tempBookingReason = tempBookingReason,
                        onTempBookingReasonChange = { tempBookingReason = it },
                        tempBatchNumber = tempBatchNumber,
                        onTempBatchNumberChange = { tempBatchNumber = it },
                        tempBestBeforeDate = tempBestBeforeDate,
                        onTempBestBeforeDateChange = { tempBestBeforeDate = it },
                        onSaveChanges = {
                            viewModel.setActiveWarehouse(tempWarehouse)
                            viewModel.setActiveBookingReason(tempBookingReason)
                            viewModel.setActiveBatchNumber(tempBatchNumber ?: "")
                            viewModel.setActiveBestBeforeDate(tempBestBeforeDate)
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
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
    tempWarehouse: Warehouse?,
    onTempWarehouseChange: (Warehouse?) -> Unit,
    tempBookingReason: BookingReason?,
    onTempBookingReasonChange: (BookingReason?) -> Unit,
    tempBatchNumber: String?,
    onTempBatchNumberChange: (String) -> Unit,
    tempBestBeforeDate: Date?,
    onTempBestBeforeDateChange: (Date?) -> Unit,
    onSaveChanges: () -> Unit,
    onCancel: () -> Unit
) {
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
                onTempWarehouseChange(it)
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
                onTempBookingReasonChange(it)
                showBookingReasonDialog = false
            }
        )
    }

    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Date(it + TimeZone.getDefault().getOffset(it))
                            onTempBestBeforeDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val readOnlyColors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warehouse Selector
            Box(modifier = Modifier.clickable { showWarehouseDialog = true }) {
                OutlinedTextField(
                    value = tempWarehouse?.name ?: "-",
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
                    value = tempBookingReason?.reason ?: "-",
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
                    value = tempBestBeforeDate?.let { mhdFormatter.format(it) } ?: "",
                    onValueChange = {},
                    label = { Text("MHD") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    colors = readOnlyColors
                )
            }
            OutlinedTextField(
                value = tempBatchNumber ?: "",
                onValueChange = onTempBatchNumberChange,
                label = { Text("Charge") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Abbrechen")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onSaveChanges) {
                Text("Übernehmen")
            }
        }
    }
}

@Composable
private fun SaveChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Änderungen speichern?") },
        text = { Text("Es gibt ungespeicherte Änderungen. Möchten Sie diese speichern?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Verwerfen")
            }
        },
    )
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
