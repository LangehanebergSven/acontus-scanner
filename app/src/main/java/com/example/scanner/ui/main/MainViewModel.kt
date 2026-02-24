package com.example.scanner.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.repository.ScanRepository
import com.example.scanner.data.sync.MasterDataSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val employeeDao: EmployeeDao,
    private val masterDataSynchronizer: MasterDataSynchronizer
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _employeeName = MutableStateFlow<String?>(null)
    val employeeName: StateFlow<String?> = _employeeName.asStateFlow()

    init {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                masterDataSynchronizer.syncIfNeeded()
            } catch (e: Exception) {
                // Log error or handle it, but don't crash the app on startup
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    var loggedInEmployeeId: String? = null
        private set

    fun findLatestProcess(employeeId: String, onResult: (Long) -> Unit) {
        loggedInEmployeeId = employeeId
        viewModelScope.launch {
            val employee = employeeDao.getEmployeeById(employeeId)
            _employeeName.value = employee?.name

            val latestProcess = scanRepository.getLatestProcessForEmployee(employeeId)
            val latestProcessId = latestProcess?.id ?: 0L
            onResult(latestProcessId)
        }
    }
}
