package com.example.scanner.ui.scanning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.model.Warehouse
import com.example.scanner.data.repository.CacheRepository
import com.example.scanner.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScanningViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val cacheRepository: CacheRepository
) : ViewModel() {

    private val processId: Long = savedStateHandle.get<Long>("processId")!!

    private val _uiState = MutableStateFlow<ScanningUiState>(ScanningUiState.Loading)
    val uiState: StateFlow<ScanningUiState> = _uiState.asStateFlow()

    // Active configuration for the NEXT scan
    private val _activeWarehouse = MutableStateFlow<Warehouse?>(null)
    val activeWarehouse: StateFlow<Warehouse?> = _activeWarehouse.asStateFlow()

    private val _activeBookingReason = MutableStateFlow<BookingReason?>(null)
    val activeBookingReason: StateFlow<BookingReason?> = _activeBookingReason.asStateFlow()

    private val _activeBestBeforeDate = MutableStateFlow<Date?>(null)
    val activeBestBeforeDate: StateFlow<Date?> = _activeBestBeforeDate.asStateFlow()

    private val _activeBatchNumber = MutableStateFlow<String?>(null)
    val activeBatchNumber: StateFlow<String?> = _activeBatchNumber.asStateFlow()

    init {
        loadScanData()
    }

    private fun loadScanData() {
        viewModelScope.launch {
            _uiState.value = ScanningUiState.Loading
            try {
                val process = scanRepository.getProcessById(processId)
                if (process == null) {
                    _uiState.value = ScanningUiState.Error("Process not found")
                    return@launch
                }

                val warehouse = cacheRepository.getWarehouseById(process.warehouseId)
                val bookingReason = cacheRepository.getBookingReasonById(process.bookingReasonId)

                if (warehouse == null || bookingReason == null) {
                    _uiState.value = ScanningUiState.Error("Configuration data missing")
                    return@launch
                }
                
                // Initialize active configuration with process defaults
                _activeWarehouse.value = warehouse
                _activeBookingReason.value = bookingReason

                val scannedItems = scanRepository.getScannedItemsForProcess(processId)

                _uiState.value = ScanningUiState.Success(
                    // These are the original process values, not the active ones
                    processWarehouse = warehouse,
                    processBookingReason = bookingReason,
                    scannedItems = scannedItems,
                    allWarehouses = cacheRepository.getWarehouses(),
                    allBookingReasons = cacheRepository.getBookingReasons()
                )
            } catch (e: Exception) {
                _uiState.value = ScanningUiState.Error("Failed to load scan data: ${e.message}")
            }
        }
    }

    fun setActiveWarehouse(warehouse: Warehouse) {
        _activeWarehouse.value = warehouse
    }
    
    fun setActiveBookingReason(bookingReason: BookingReason) {
        _activeBookingReason.value = bookingReason
    }

    fun setActiveBestBeforeDate(date: Date?) {
        _activeBestBeforeDate.value = date
    }

    fun setActiveBatchNumber(batchNumber: String?) {
        _activeBatchNumber.value = batchNumber?.takeIf { it.isNotBlank() }
    }
}

sealed interface ScanningUiState {
    object Loading : ScanningUiState
    data class Success(
        val processWarehouse: Warehouse,
        val processBookingReason: BookingReason,
        val scannedItems: List<ScannedItem>,
        val allWarehouses: List<Warehouse>,
        val allBookingReasons: List<BookingReason>
    ) : ScanningUiState
    data class Error(val message: String) : ScanningUiState
}
