package com.example.scanner.ui.scanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.ScanProcess
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
    private val scanRepository: ScanRepository,
    private val cacheRepository: CacheRepository
) : ViewModel() {

    // TODO: Replace with actual logged-in employee ID
    private val employeeId = "12345"

    private val _uiState = MutableStateFlow<ScanningUiState>(ScanningUiState.Loading)
    val uiState: StateFlow<ScanningUiState> = _uiState.asStateFlow()
    
    // ... active configuration StateFlows remain the same ...
    private val _activeWarehouse = MutableStateFlow<Warehouse?>(null)
    val activeWarehouse: StateFlow<Warehouse?> = _activeWarehouse.asStateFlow()

    private val _activeBookingReason = MutableStateFlow<BookingReason?>(null)
    val activeBookingReason: StateFlow<BookingReason?> = _activeBookingReason.asStateFlow()

    private val _activeBestBeforeDate = MutableStateFlow<Date?>(null)
    val activeBestBeforeDate: StateFlow<Date?> = _activeBestBeforeDate.asStateFlow()

    private val _activeBatchNumber = MutableStateFlow<String?>(null)
    val activeBatchNumber: StateFlow<String?> = _activeBatchNumber.asStateFlow()


    init {
        loadActiveProcess()
    }

    fun loadActiveProcess() {
        viewModelScope.launch {
            _uiState.value = ScanningUiState.Loading
            try {
                val process = scanRepository.getLatestProcessForEmployee(employeeId)
                if (process == null) {
                    _uiState.value = ScanningUiState.NoProcess
                    return@launch
                }
                loadScanData(process)
            } catch (e: Exception) {
                _uiState.value = ScanningUiState.Error("Failed to load active process: ${e.message}")
            }
        }
    }

    private suspend fun loadScanData(process: ScanProcess) {
        val warehouse = cacheRepository.getWarehouseById(process.warehouseId)
        val bookingReason = cacheRepository.getBookingReasonById(process.bookingReasonId)

        if (warehouse == null || bookingReason == null) {
            _uiState.value = ScanningUiState.Error("Configuration data missing")
            return
        }

        // Initialize active configuration with process defaults
        _activeWarehouse.value = warehouse
        _activeBookingReason.value = bookingReason
        _activeBestBeforeDate.value = null
        _activeBatchNumber.value = null

        val scannedItems = scanRepository.getScannedItemsForProcess(process.id)

        _uiState.value = ScanningUiState.Success(
            process = process,
            processWarehouse = warehouse,
            processBookingReason = bookingReason,
            scannedItems = scannedItems,
            allWarehouses = cacheRepository.getWarehouses(),
            allBookingReasons = cacheRepository.getBookingReasons()
        )
    }
    
    fun cancelProcess() {
        viewModelScope.launch {
            // TODO: Implement process cancellation logic
            // This will likely involve deleting the ScanProcess and ScannedItems
            _uiState.value = ScanningUiState.NoProcess
        }
    }
    
    fun submitProcess() {
        viewModelScope.launch {
            // TODO: Implement process submission logic from Phase 5
        }
    }

    // ... setActive methods remain the same ...
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
    object NoProcess : ScanningUiState
    data class Success(
        val process: ScanProcess,
        val processWarehouse: Warehouse,
        val processBookingReason: BookingReason,
        val scannedItems: List<ScannedItem>,
        val allWarehouses: List<Warehouse>,
        val allBookingReasons: List<BookingReason>
    ) : ScanningUiState
    data class Error(val message: String) : ScanningUiState
}
