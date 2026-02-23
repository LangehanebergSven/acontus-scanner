package com.example.scanner.ui.process

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Warehouse
import com.example.scanner.data.repository.ScanRepository
import com.example.scanner.data.sync.MasterDataSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessConfigurationViewModel @Inject constructor(
    private val warehouseDao: WarehouseDao,
    private val bookingReasonDao: BookingReasonDao,
    private val scanRepository: ScanRepository,
    private val masterDataSynchronizer: MasterDataSynchronizer
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProcessUiState>(ProcessUiState.Loading)
    val uiState: StateFlow<ProcessUiState> = _uiState.asStateFlow()

    private val _selectedWarehouse = MutableStateFlow<Warehouse?>(null)
    val selectedWarehouse: StateFlow<Warehouse?> = _selectedWarehouse.asStateFlow()

    private val _selectedBookingReason = MutableStateFlow<BookingReason?>(null)
    val selectedBookingReason: StateFlow<BookingReason?> = _selectedBookingReason.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Ensure data is available (sync or dummy data)
                masterDataSynchronizer.syncIfNeeded()
                
                val warehouses = warehouseDao.getAll()
                val bookingReasons = bookingReasonDao.getAll()
                
                _uiState.value = ProcessUiState.Success(
                    warehouses = warehouses,
                    bookingReasons = bookingReasons
                )
                // Set default selections
                _selectedWarehouse.value = warehouses.firstOrNull()
                _selectedBookingReason.value = bookingReasons.firstOrNull()
            } catch (e: Exception) {
                Log.e("ProcessConfigurationViewModel.loadInitialData", "Failed to load data", e)
                _uiState.value = ProcessUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun selectWarehouse(warehouse: Warehouse) {
        _selectedWarehouse.value = warehouse
    }

    fun selectBookingReason(bookingReason: BookingReason) {
        _selectedBookingReason.value = bookingReason
    }

    fun startProcess(employeeId: String, onProcessStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val warehouse = _selectedWarehouse.value
            val bookingReason = _selectedBookingReason.value
            
            if (warehouse != null && bookingReason != null) {
                val processId = scanRepository.startNewProcess(
                    employeeId = employeeId,
                    warehouseId = warehouse.warehouseId,
                    bookingReasonId = bookingReason.bookingReasonId
                )
                onProcessStarted(processId)
            } else {
                // Handle error - e.g., show a message to the user
            }
        }
    }
}

sealed interface ProcessUiState {
    object Loading : ProcessUiState
    data class Success(
        val warehouses: List<Warehouse>,
        val bookingReasons: List<BookingReason>
    ) : ProcessUiState
    data class Error(val message: String) : ProcessUiState
}
