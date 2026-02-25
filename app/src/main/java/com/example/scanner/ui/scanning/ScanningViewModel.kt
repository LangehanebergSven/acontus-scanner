package com.example.scanner.ui.scanning

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.ScanProcess
import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.model.SearchResult
import com.example.scanner.data.model.Warehouse
import com.example.scanner.data.repository.ScanRepository
import com.example.scanner.data.repository.SyncRepository
import com.example.scanner.domain.SearchItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScanningViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val syncRepository: SyncRepository,
    private val warehouseDao: WarehouseDao,
    private val bookingReasonDao: BookingReasonDao,
    private val articleDao: ArticleDao,
    private val materialDao: MaterialDao,
    private val searchItemsUseCase: SearchItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanningUiState>(ScanningUiState.Loading)
    val uiState: StateFlow<ScanningUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastUpdatedItemId: Long? = null
    private val mhdFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

    var isSearchFieldFocused: Boolean = false

    init {
        savedStateHandle.getStateFlow("processId", 0L)
            .onEach { processId ->
                loadActiveProcess(processId)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
            
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300) // Debounce
                val results = searchItemsUseCase(query)
                val newState = _uiState.value
                if (newState is ScanningUiState.Success) {
                    _uiState.value = newState.copy(searchResults = results)
                }
            }
        }
    }

    fun onSearchResultSelected(result: SearchResult) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(
                showQuantityDialog = true,
                selectedSearchResult = result,
                editingItem = null
            )
        }
    }

    fun onEditItemClicked(item: ScannedItemUi) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            if (currentState.isMultiSelectMode) {
                toggleSelection(item.id)
            } else {
                _uiState.value = currentState.copy(
                    showQuantityDialog = true,
                    editingItem = item,
                    selectedSearchResult = null
                )
            }
        }
    }

    fun onItemLongClicked(item: ScannedItemUi) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            if (!currentState.isMultiSelectMode) {
                _uiState.value = currentState.copy(
                    isMultiSelectMode = true,
                    selectedItemIds = setOf(item.id)
                )
            } else {
                toggleSelection(item.id)
            }
        }
    }

    fun onExternalBarcodeScanned(barcode: String) {
        if (isSearchFieldFocused) {
            onSearchQueryChanged(barcode)
        } else {
            processDirectScan(barcode)
        }
    }

    private fun processDirectScan(barcode: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? ScanningUiState.Success ?: return@launch

            // 1. Explizit nur nach exakter EAN suchen
            val article = articleDao.getArticleByEan(barcode)
            val material = materialDao.getMaterialByEan(barcode)

            val (articleId, materialId) = when {
                article != null -> article.articleId to null
                material != null -> null to material.materialId
                else -> {
                    Log.w("Scanner", "Kein Artikel oder Material gefunden für EAN: $barcode")
                    _uiState.value = currentState.copy(scanError = "Kein Artikel oder Material gefunden für EAN: $barcode")
                    return@launch
                }
            }

            // 2. Artikel mit Menge 1 hinzufügen oder vorhandenen hochzählen
            val existingItem = currentState.rawScannedItems.find {
                it.articleId == articleId &&
                it.materialId == materialId &&
                it.warehouseId == (currentState.activeWarehouse?.warehouseId ?: currentState.processWarehouse.warehouseId) &&
                it.bookingReasonId == (currentState.activeBookingReason?.bookingReasonId ?: currentState.processBookingReason.bookingReasonId) &&
                it.batchNumber == currentState.activeBatchNumber &&
                it.bestBeforeDate == currentState.activeBestBeforeDate
            }

            if (existingItem != null) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                scanRepository.updateScannedItem(updatedItem)
                lastUpdatedItemId = updatedItem.id
            } else {
                val newItem = ScannedItem(
                    id = 0,
                    scanProcessId = currentState.process.id,
                    articleId = articleId,
                    materialId = materialId,
                    quantity = 1,
                    contentQuantity = null,
                    warehouseId = currentState.activeWarehouse?.warehouseId ?: currentState.processWarehouse.warehouseId,
                    bookingReasonId = currentState.activeBookingReason?.bookingReasonId ?: currentState.processBookingReason.bookingReasonId,
                    batchNumber = currentState.activeBatchNumber,
                    bestBeforeDate = currentState.activeBestBeforeDate,
                    scannedAt = Date()
                )
                lastUpdatedItemId = scanRepository.addScannedItem(newItem)
            }

            loadActiveProcess(currentState.process.id)
        }
    }

    private fun toggleSelection(itemId: Long) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            val newSelection = currentState.selectedItemIds.toMutableSet()
            if (newSelection.contains(itemId)) {
                newSelection.remove(itemId)
            } else {
                newSelection.add(itemId)
            }
            
            if (newSelection.isEmpty()) {
                _uiState.value = currentState.copy(
                    isMultiSelectMode = false,
                    selectedItemIds = emptySet()
                )
            } else {
                _uiState.value = currentState.copy(selectedItemIds = newSelection)
            }
        }
    }

    fun onClearSelection() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(
                isMultiSelectMode = false,
                selectedItemIds = emptySet()
            )
        }
    }

    fun onDeleteSelectedItems() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            viewModelScope.launch {
                val itemsToDelete = currentState.rawScannedItems.filter { currentState.selectedItemIds.contains(it.id) }
                scanRepository.deleteScannedItems(itemsToDelete)
                onClearSelection()
                loadActiveProcess(currentState.process.id)
            }
        }
    }

    fun onBulkEditItemsClicked() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(showBulkEditDialog = true)
        }
    }

    fun onBulkEditDialogDismissed() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(showBulkEditDialog = false)
        }
    }

    fun onBulkEditConfirmed(
        warehouse: Warehouse?,
        bookingReason: BookingReason?,
        batchNumber: String?,
        bestBeforeDate: Date?
    ) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            viewModelScope.launch {
                val itemsToUpdate = currentState.rawScannedItems.filter { currentState.selectedItemIds.contains(it.id) }
                
                val updatedItems = itemsToUpdate.map { item ->
                    item.copy(
                        warehouseId = warehouse?.warehouseId ?: item.warehouseId,
                        bookingReasonId = bookingReason?.bookingReasonId ?: item.bookingReasonId,
                        batchNumber = batchNumber, 
                        bestBeforeDate = bestBeforeDate 
                    )
                }
                
                // Batch update
                for (item in updatedItems) {
                    scanRepository.updateScannedItem(item)
                }
                
                onClearSelection()
                onBulkEditDialogDismissed()
                loadActiveProcess(currentState.process.id)
            }
        }
    }

    fun onDeleteItemClicked(item: ScannedItemUi) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            viewModelScope.launch {
                val itemToDelete = currentState.rawScannedItems.find { it.id == item.id }
                if (itemToDelete != null) {
                    scanRepository.deleteScannedItems(listOf(itemToDelete))
                    loadActiveProcess(currentState.process.id)
                }
            }
        }
    }

    fun onQuantityDialogDismissed() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(
                showQuantityDialog = false,
                selectedSearchResult = null,
                editingItem = null
            )
        }
    }

    fun onQuantityConfirmed(quantity: Int) {
        val currentState = _uiState.value as? ScanningUiState.Success ?: return

        viewModelScope.launch {
            if (currentState.editingItem != null) {
                val existingItem = currentState.rawScannedItems.find { it.id == currentState.editingItem.id }
                if (existingItem != null) {
                    val updatedItem = existingItem.copy(quantity = quantity)
                    scanRepository.updateScannedItem(updatedItem)
                    lastUpdatedItemId = updatedItem.id
                }
            } else if (currentState.selectedSearchResult != null) {
                val result = currentState.selectedSearchResult
                val (articleId, materialId) = when(result) {
                    is SearchResult.ArticleResult -> result.article.articleId to null
                    is SearchResult.MaterialResult -> null to result.material.materialId
                }

                val existingItem = currentState.rawScannedItems.find { 
                    it.articleId == articleId &&
                    it.materialId == materialId &&
                    it.warehouseId == (currentState.activeWarehouse?.warehouseId ?: currentState.processWarehouse.warehouseId) &&
                    it.bookingReasonId == (currentState.activeBookingReason?.bookingReasonId ?: currentState.processBookingReason.bookingReasonId) &&
                    it.batchNumber == currentState.activeBatchNumber &&
                    it.bestBeforeDate == currentState.activeBestBeforeDate
                }

                if (existingItem != null) {
                    val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
                    scanRepository.updateScannedItem(updatedItem)
                    lastUpdatedItemId = updatedItem.id
                } else {
                    val newItem = ScannedItem(
                        id = 0,
                        scanProcessId = currentState.process.id,
                        articleId = articleId,
                        materialId = materialId,
                        quantity = quantity,
                        contentQuantity = null,
                        warehouseId = currentState.activeWarehouse?.warehouseId ?: currentState.processWarehouse.warehouseId,
                        bookingReasonId = currentState.activeBookingReason?.bookingReasonId ?: currentState.processBookingReason.bookingReasonId,
                        batchNumber = currentState.activeBatchNumber,
                        bestBeforeDate = currentState.activeBestBeforeDate,
                        scannedAt = Date()
                    )
                    lastUpdatedItemId = scanRepository.addScannedItem(newItem)
                }
            }
            
            loadActiveProcess(currentState.process.id)
        }
        
        _uiState.value = currentState.copy(
            showQuantityDialog = false,
            selectedSearchResult = null,
            editingItem = null,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun loadActiveProcess(processId: Long) {
        viewModelScope.launch {
            _uiState.value = ScanningUiState.Loading
            try {
                if (processId == 0L) {
                    _uiState.value = ScanningUiState.NoProcess()
                    return@launch
                }
                val process = scanRepository.getProcessById(processId)
                if (process == null) {
                    _uiState.value = ScanningUiState.NoProcess()
                    return@launch
                }
                loadScanData(process)
            } catch (e: Exception) {
                Log.e("ScanningViewModel", "Failed to load active process", e)
                _uiState.value = ScanningUiState.Error("Fehler beim Laden des Prozesses: ${e.message}")
            }
        }
    }

    private suspend fun loadScanData(process: ScanProcess) {
        val warehouse = warehouseDao.getById(process.warehouseId)
        val bookingReason = bookingReasonDao.getById(process.bookingReasonId)

        if (warehouse == null || bookingReason == null) {
            _uiState.value = ScanningUiState.NoProcess()
            return
        }

        val allWarehouses = warehouseDao.getAll()
        val allBookingReasons = bookingReasonDao.getAll()
        val rawScannedItems = scanRepository.getScannedItemsForProcess(process.id)
        val uiItems = mapToUiModels(rawScannedItems, allWarehouses, allBookingReasons)

        val previousState = _uiState.value as? ScanningUiState.Success

        _uiState.value = ScanningUiState.Success(
            process = process,
            processWarehouse = warehouse,
            processBookingReason = bookingReason,
            rawScannedItems = rawScannedItems,
            scannedItemsUi = uiItems,
            allWarehouses = allWarehouses,
            allBookingReasons = allBookingReasons,
            activeWarehouse = previousState?.activeWarehouse ?: warehouse,
            activeBookingReason = previousState?.activeBookingReason ?: bookingReason,
            activeBestBeforeDate = previousState?.activeBestBeforeDate ?: process.bestBeforeDate,
            activeBatchNumber = previousState?.activeBatchNumber ?: process.batchNumber,
            searchQuery = "",
            searchResults = emptyList(),
            showQuantityDialog = false,
            selectedSearchResult = null,
            editingItem = null,
            lastScannedItemId = lastUpdatedItemId,
            isMultiSelectMode = previousState?.isMultiSelectMode ?: false,
            selectedItemIds = previousState?.selectedItemIds ?: emptySet(),
            isSubmitting = false,
            submitError = null,
            scanError = null,
            showBulkEditDialog = false
        )
        
        if (lastUpdatedItemId != null) {
            delay(2000)
            val currentState = _uiState.value
            if (currentState is ScanningUiState.Success) {
                _uiState.value = currentState.copy(lastScannedItemId = null)
            }
            lastUpdatedItemId = null
        }
    }

    private suspend fun mapToUiModels(
        items: List<ScannedItem>,
        warehouses: List<Warehouse>,
        bookingReasons: List<BookingReason>
    ): List<ScannedItemUi> {
        val warehouseMap = warehouses.associateBy { it.warehouseId }
        val reasonMap = bookingReasons.associateBy { it.bookingReasonId }

        return items.map { item ->
            val (name, type, idString) = if (item.articleId != null) {
                val article = articleDao.getArticleById(item.articleId)
                Triple(article?.name ?: "Unknown Article", "Article", item.articleId)
            } else if (item.materialId != null) {
                val material = materialDao.getMaterialById(item.materialId)
                Triple(material?.name ?: "Unknown Material", "Material", item.materialId)
            } else {
                Triple("Unknown Item", "Unknown", "")
            }
            
            val whName = warehouseMap[item.warehouseId]?.name ?: item.warehouseId
            val reasonName = reasonMap[item.bookingReasonId]?.reason ?: item.bookingReasonId
            val mhd = item.bestBeforeDate?.let { mhdFormatter.format(it) }

            ScannedItemUi(
                id = item.id,
                itemId = idString,
                itemName = name,
                itemType = type,
                quantity = item.quantity,
                scannedAt = item.scannedAt,
                warehouseName = whName,
                bookingReasonName = reasonName,
                batchNumber = item.batchNumber,
                bestBeforeDate = mhd
            )
        }
    }

    fun cancelProcess() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            viewModelScope.launch {
                scanRepository.deleteProcess(currentState.process)
                _uiState.value = ScanningUiState.NoProcess()
            }
        }
    }

    fun clearSubmitError() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(submitError = null)
        }
    }

    fun clearScanError() {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(scanError = null)
        }
    }

    fun submitProcess() {
        val currentState = _uiState.value as? ScanningUiState.Success ?: return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true, submitError = null)
            
            try {
                // Submit items using SyncRepository
                val itemsToSubmit = currentState.rawScannedItems.toList()
                for (item in itemsToSubmit) {
                    syncRepository.submitScannedItem(item, currentState.process.employeeId)
                    // If successful, delete locally
                    scanRepository.deleteScannedItems(listOf(item))
                }
                
                // Process submitted successfully (or logged for offline), delete local process
                scanRepository.deleteProcess(currentState.process)
                _uiState.value = ScanningUiState.NoProcess("Daten erfolgreich gesendet!")
                
            } catch (e: Exception) {
                Log.e("ScanningViewModel", "Error submitting process", e)
                
                // Reload remaining items
                val process = scanRepository.getProcessById(currentState.process.id)
                if (process != null) {
                    loadScanData(process)
                    
                    // Restore error message
                    val refreshedState = _uiState.value
                    if (refreshedState is ScanningUiState.Success) {
                        _uiState.value = refreshedState.copy(
                            isSubmitting = false,
                            submitError = "Fehler beim Senden: ${e.localizedMessage ?: e.message}"
                        )
                    }
                } else {
                    _uiState.value = ScanningUiState.Error("Prozess nicht mehr gefunden: ${e.message}")
                }
            }
        }
    }

    private fun saveConfigurationChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ScanningUiState.Success) {
                val updatedProcess = currentState.process.copy(
                    warehouseId = currentState.activeWarehouse?.warehouseId ?: currentState.process.warehouseId,
                    bookingReasonId = currentState.activeBookingReason?.bookingReasonId ?: currentState.process.bookingReasonId,
                    batchNumber = currentState.activeBatchNumber,
                    bestBeforeDate = currentState.activeBestBeforeDate
                )
                scanRepository.updateProcess(updatedProcess)
            }
        }
    }

    fun setActiveWarehouse(warehouse: Warehouse?) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(activeWarehouse = warehouse)
            saveConfigurationChanges()
        }
    }

    fun setActiveBookingReason(bookingReason: BookingReason?) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(activeBookingReason = bookingReason)
            saveConfigurationChanges()
        }
    }

    fun setActiveBestBeforeDate(date: Date?) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            _uiState.value = currentState.copy(activeBestBeforeDate = date)
            saveConfigurationChanges()
        }
    }

    fun setActiveBatchNumber(batchNumber: String?) {
        val currentState = _uiState.value
        if (currentState is ScanningUiState.Success) {
            val finalBatchNumber = batchNumber?.takeIf { it.isNotBlank() }
            _uiState.value = currentState.copy(activeBatchNumber = finalBatchNumber)
            saveConfigurationChanges()
        }
    }
}

sealed interface ScanningUiState {
    object Loading : ScanningUiState
    data class NoProcess(val successMessage: String? = null) : ScanningUiState
    data class Success(
        val process: ScanProcess,
        val processWarehouse: Warehouse,
        val processBookingReason: BookingReason,
        val rawScannedItems: List<ScannedItem>,
        val scannedItemsUi: List<ScannedItemUi>,
        val allWarehouses: List<Warehouse>,
        val allBookingReasons: List<BookingReason>,
        val activeWarehouse: Warehouse?,
        val activeBookingReason: BookingReason?,
        val activeBestBeforeDate: Date?,
        val activeBatchNumber: String?,
        val searchQuery: String = "",
        val searchResults: List<SearchResult> = emptyList(),
        val showQuantityDialog: Boolean = false,
        val selectedSearchResult: SearchResult? = null,
        val editingItem: ScannedItemUi? = null,
        val lastScannedItemId: Long? = null,
        // Multi-select state
        val isMultiSelectMode: Boolean = false,
        val selectedItemIds: Set<Long> = emptySet(),
        val showBulkEditDialog: Boolean = false,
        // Submission state
        val isSubmitting: Boolean = false,
        val submitError: String? = null,
        val scanError: String? = null
    ) : ScanningUiState

    data class Error(val message: String) : ScanningUiState
}
