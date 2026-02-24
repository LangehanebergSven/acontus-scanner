package com.example.scanner.ui.scanning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.SearchResult
import com.example.scanner.data.model.Warehouse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanningScreen(
    rootNavController: NavController,
    viewModel: ScanningViewModel,
    onStartNewProcess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    // 1. Broadcast Receiver für den Scanner
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val barcode = intent?.getStringExtra("com.keyence.autoid.scanmanagersdk.data")
                val codeType = intent?.getStringExtra("com.keyence.autoid.scanmanagersdk.code_type")

                if (codeType != "UPC/EAN/JAN")
                    return

                barcode?.let { code ->
                    Log.e("ScanningScreen", "onReceive: $code")
                    viewModel.onExternalBarcodeScanned(code)
                }
            }
        }

        val filter = IntentFilter("com.acontus.SCAN")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            val state = uiState
            if (state is ScanningUiState.Success && state.isMultiSelectMode) {
                TopAppBar(
                    title = { Text("${state.selectedItemIds.size} ausgewählt") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::onClearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::onBulkEditItemsClicked) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                        }
                        IconButton(onClick = viewModel::onDeleteSelectedItems) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            val state = uiState
            if (state is ScanningUiState.Success && !state.isMultiSelectMode) {
                Box {
                    FloatingActionButton(
                        onClick = { isFabMenuExpanded = true }
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(Icons.Default.MoreVert, contentDescription = "Aktionen")
                        }
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
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Senden") },
                            onClick = {
                                viewModel.submitProcess()
                                isFabMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
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
                .consumeWindowInsets(padding)
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
                        onStartProcess = onStartNewProcess
                    )
                }
                is ScanningUiState.Success -> {
                    // Dialog handling logic
                    if (state.showQuantityDialog) {
                        val itemName = state.editingItem?.itemName 
                            ?: state.selectedSearchResult?.let { 
                                when(it) {
                                    is SearchResult.ArticleResult -> it.article.name
                                    is SearchResult.MaterialResult -> it.material.name
                                }
                            } ?: ""
                            
                        val initialQuantity = state.editingItem?.quantity ?: 1
                        val confirmText = if (state.editingItem != null) "Speichern" else "Hinzufügen"

                        QuantityDialog(
                            itemName = itemName,
                            initialQuantity = initialQuantity,
                            confirmButtonText = confirmText,
                            onConfirm = viewModel::onQuantityConfirmed,
                            onDismiss = viewModel::onQuantityDialogDismissed
                        )
                    }

                    if (state.showBulkEditDialog) {
                        BulkEditDialog(
                            initialWarehouse = state.activeWarehouse,
                            initialBookingReason = state.activeBookingReason,
                            initialBatchNumber = state.activeBatchNumber,
                            initialBestBeforeDate = state.activeBestBeforeDate,
                            allWarehouses = state.allWarehouses,
                            allBookingReasons = state.allBookingReasons,
                            onConfirm = viewModel::onBulkEditConfirmed,
                            onDismiss = viewModel::onBulkEditDialogDismissed
                        )
                    }
                    
                    if (state.submitError != null) {
                        AlertDialog(
                            onDismissRequest = viewModel::clearSubmitError,
                            title = { Text("Fehler beim Senden") },
                            text = { Text(state.submitError) },
                            confirmButton = {
                                TextButton(onClick = viewModel::clearSubmitError) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    if (state.scanError != null) {
                        AlertDialog(
                            onDismissRequest = viewModel::clearScanError,
                            title = { Text("Scan Fehler") },
                            text = { Text(state.scanError) },
                            confirmButton = {
                                TextButton(onClick = viewModel::clearScanError) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    var isSearchFocused by remember { mutableStateOf(false) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = !isSearchFocused && state.searchQuery.isEmpty() && !state.isMultiSelectMode,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    CompactConfigurationHeader(
                                        warehouse = state.activeWarehouse,
                                        bookingReason = state.activeBookingReason,
                                        batchNumber = state.activeBatchNumber,
                                        bestBeforeDate = state.activeBestBeforeDate
                                    )
                                }
                            }
                        }

                        item {
                            if (!state.isMultiSelectMode) {
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchBar(
                                    query = state.searchQuery,
                                    onQueryChanged = viewModel::onSearchQueryChanged,
                                    onFocusChanged = { 
                                        isSearchFocused = it 
                                        viewModel.isSearchFieldFocused = it
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        if (state.searchResults.isNotEmpty() && !state.isMultiSelectMode) {
                            SearchResultsList(
                                results = state.searchResults,
                                onResultClick = viewModel::onSearchResultSelected
                            )
                        } else {
                            item {
                                if (!state.isMultiSelectMode) {
                                    HorizontalDivider()
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (state.scannedItemsUi.isEmpty()) {
                                item {
                                    EmptyState()
                                }
                            } else {
                                ScannedItemsList(
                                    items = state.scannedItemsUi,
                                    lastScannedItemId = state.lastScannedItemId,
                                    isMultiSelectMode = state.isMultiSelectMode,
                                    selectedItemIds = state.selectedItemIds,
                                    onEditItem = viewModel::onEditItemClicked,
                                    onLongClickItem = viewModel::onItemLongClicked,
                                    onDeleteItem = viewModel::onDeleteItemClicked
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Artikel/Material suchen") },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it.isFocused) },
        trailingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        }
    )
}

fun LazyListScope.SearchResultsList(
    results: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit
) {
    items(results) { result ->
        ListItem(
            headlineContent = {
                when(result) {
                    is SearchResult.ArticleResult -> Text(result.article.name)
                    is SearchResult.MaterialResult -> Text(result.material.name)
                }
            },
            supportingContent = {
                when(result) {
                    is SearchResult.ArticleResult -> Text("Artikel: ${result.article.articleId}")
                    is SearchResult.MaterialResult -> Text("Material: ${result.material.materialId}")
                }
            },
            modifier = Modifier.clickable { onResultClick(result) }
        )
        HorizontalDivider()
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
    bestBeforeDate: Date?
) {
    val mhdFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            ConfigItem(label = "Lager:", value = warehouse?.name ?: "-")
            ConfigItem(label = "Grund:", value = bookingReason?.reason ?: "-")
            ConfigItem(label = "Charge:", value = batchNumber ?: "-")
            ConfigItem(label = "MHD:", value = bestBeforeDate?.let { mhdFormatter.format(it) } ?: "-")
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Noch keine Artikel gescannt.",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun LazyListScope.ScannedItemsList(
    items: List<ScannedItemUi>,
    lastScannedItemId: Long? = null,
    isMultiSelectMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onEditItem: (ScannedItemUi) -> Unit,
    onLongClickItem: (ScannedItemUi) -> Unit,
    onDeleteItem: (ScannedItemUi) -> Unit
) {
    items(items, key = { it.id }) { item ->
        val isHighlighted = item.id == lastScannedItemId
        val isSelected = selectedItemIds.contains(item.id)
        
        val backgroundColor by animateColorAsState(
            targetValue = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isHighlighted -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            },
            animationSpec = tween(durationMillis = 300)
        )

        if (!isMultiSelectMode) {
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteItem(item)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color = MaterialTheme.colorScheme.errorContainer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Löschen",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                },
                content = {
                    ScannedItemRow(
                        item = item,
                        backgroundColor = backgroundColor,
                        isMultiSelectMode = false,
                        isSelected = false,
                        onEditItem = onEditItem,
                        onLongClickItem = onLongClickItem
                    )
                },
                enableDismissFromStartToEnd = false
            )
        } else {
            ScannedItemRow(
                item = item,
                backgroundColor = backgroundColor,
                isMultiSelectMode = true,
                isSelected = isSelected,
                onEditItem = onEditItem,
                onLongClickItem = onLongClickItem
            )
        }
        HorizontalDivider()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScannedItemRow(
    item: ScannedItemUi,
    backgroundColor: Color,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onEditItem: (ScannedItemUi) -> Unit,
    onLongClickItem: (ScannedItemUi) -> Unit
) {
    ListItem(
        headlineContent = { Text(item.itemName) },
        leadingContent = if (isMultiSelectMode) {
            {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            }
        } else null,
        supportingContent = {
            Column {
                Text("ID: ${item.itemId} | ${item.itemType}")
                Text(
                    text = "Lager: ${item.warehouseName} | Grund: ${item.bookingReasonName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!item.batchNumber.isNullOrEmpty() || !item.bestBeforeDate.isNullOrEmpty()) {
                    Text(
                        text = "Charge: ${item.batchNumber ?: "-"} | MHD: ${item.bestBeforeDate ?: "-"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = "${item.quantity}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = Modifier
            .background(backgroundColor)
            .combinedClickable(
                onClick = { onEditItem(item) },
                onLongClick = { onLongClickItem(item) }
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditDialog(
    initialWarehouse: Warehouse?,
    initialBookingReason: BookingReason?,
    initialBatchNumber: String?,
    initialBestBeforeDate: Date?,
    allWarehouses: List<Warehouse>,
    allBookingReasons: List<BookingReason>,
    onConfirm: (Warehouse?, BookingReason?, String?, Date?) -> Unit,
    onDismiss: () -> Unit
) {
    var warehouse by remember { mutableStateOf(initialWarehouse) }
    var bookingReason by remember { mutableStateOf(initialBookingReason) }
    var batchNumber by remember { mutableStateOf(initialBatchNumber) }
    var bestBeforeDate by remember { mutableStateOf(initialBestBeforeDate) }

    var showWarehouseDialog by remember { mutableStateOf(false) }
    var showBookingReasonDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showWarehouseDialog) {
        SelectionDialog(
            title = "Lager wählen",
            items = allWarehouses,
            itemText = { it.name },
            onDismiss = { showWarehouseDialog = false },
            onSelect = {
                warehouse = it
                showWarehouseDialog = false
            }
        )
    }

    if (showBookingReasonDialog) {
        SelectionDialog(
            title = "Buchungsgrund wählen",
            items = allBookingReasons,
            itemText = { it.reason },
            onDismiss = { showBookingReasonDialog = false },
            onSelect = {
                bookingReason = it
                showBookingReasonDialog = false
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = bestBeforeDate?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            bestBeforeDate = Date(it + TimeZone.getDefault().getOffset(it))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auswahl bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Warehouse
                 OutlinedTextField(
                    value = warehouse?.name ?: "-",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lager") },
                    modifier = Modifier.fillMaxWidth().clickable { showWarehouseDialog = true },
                    enabled = false,
                     colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Booking Reason
                 OutlinedTextField(
                    value = bookingReason?.reason ?: "-",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grund") },
                    modifier = Modifier.fillMaxWidth().clickable { showBookingReasonDialog = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Batch
                OutlinedTextField(
                    value = batchNumber ?: "",
                    onValueChange = { batchNumber = it },
                    label = { Text("Charge") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date
                val mhdFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
                OutlinedTextField(
                    value = bestBeforeDate?.let { mhdFormatter.format(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("MHD") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(warehouse, bookingReason, batchNumber, bestBeforeDate) }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    itemText: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(items) { item ->
                        Text(
                            text = itemText(item),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                                .padding(vertical = 12.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
