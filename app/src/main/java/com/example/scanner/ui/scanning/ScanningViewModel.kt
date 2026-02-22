package com.example.scanner.ui.scanning

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScanningViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanRepository: ScanRepository,
    private val cacheRepository: CacheRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanningUiState>(ScanningUiState.Loading)
    val uiState: StateFlow<ScanningUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.getStateFlow("processId", 0L)
            .onEach { processId ->
                loadActiveProcess(processId)
            }
            .launchIn(viewModelScope)
    }

    fun loadActiveProcess(processId: Long) {
        viewModelScope.launch {
            _uiState.value = ScanningUiState.Loading
            try {
                if (processId == 0L) {
                    _uiState.value = ScanningUiState.NoProcess
                    return@launch
                }

                val process = scanRepository.getProcessById(processId)
                if (process == null) {
                    _uiState.value = ScanningUiState.NoProcess
                    return@launch
                }
                loadScanData(process)
            } catch (e: Exception) {
                Log.e("ScanningViewModel", "Failed to load active process", e)
                _uiState.value = ScanningUiState.Error("Failed to load active process: ${e.message}")
            }
        }
    }

    private suspend fun loadScanData(process: ScanProcess) {
        val warehouse = cacheRepository.getWarehouseById(process.warehouseId)
        val bookingReason = cacheRepository.getBookingReasonById(process.bookingReasonId)

        if (warehouse == null || bookingReason == null) {
            _uiState.value = ScanningUiState.NoProcess
            return
        }

        val scannedItems = scanRepository.getScannedItemsForProcess(process.id)

        _uiState.value = ScanningUiState.Success(
            process = process,
            processWarehouse = warehouse,
            processBookingReason = bookingReason,
            scannedItems = scannedItems,
            allWarehouses = cacheRepository.getWarehouses(),
            allBookingReasons = cacheRepository.getBookingReasons(),
            activeWarehouse = warehouse,
            activeBookingReason = bookingReason,
            activeBestBeforeDate = process.bestBeforeDate,
            activeBatchNumber = process.batchNumber
        )
    }

    fun cancelProcess() {
        viewModelScope.launch {
            // TODO: Implement process cancellation logic
            _uiState.value = ScanningUiState.NoProcess
        }
    }

    fun submitProcess() {
        viewModelScope.launch {
            // TODO: Implement process submission logic
        }
    }

    private fun saveConfigurationChanges() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ScanningUiState.Success) {
                // Create an updated process object from the current state
                val updatedProcess = currentState.process.copy(
                    warehouseId = currentState.activeWarehouse?.warehouseId ?: currentState.process.warehouseId,
                    bookingReasonId = currentState.activeBookingReason?.bookingReasonId ?: currentState.process.bookingReasonId,
                    batchNumber = currentState.activeBatchNumber,
                    bestBeforeDate = currentState.activeBestBeforeDate
                )
                // Save the changes to the database
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
    object NoProcess : ScanningUiState
    data class Success(
        val process: ScanProcess,
        val processWarehouse: Warehouse,
        val processBookingReason: BookingReason,
        val scannedItems: List<ScannedItem>,
        val allWarehouses: List<Warehouse>,
        val allBookingReasons: List<BookingReason>,
        // Active configuration
        val activeWarehouse: Warehouse?,
        val activeBookingReason: BookingReason?,
        val activeBestBeforeDate: Date?,
        val activeBatchNumber: String?
    ) : ScanningUiState

    data class Error(val message: String) : ScanningUiState
}
