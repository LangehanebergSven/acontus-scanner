package com.example.scanner.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    var loggedInEmployeeId: String? = null
        private set

    fun findLatestProcess(employeeId: String, onResult: (Long) -> Unit) {
        loggedInEmployeeId = employeeId
        viewModelScope.launch {
            val latestProcess = scanRepository.getLatestProcessForEmployee(employeeId)
            val latestProcessId = latestProcess?.id ?: 0L
            onResult(latestProcessId)
        }
    }
}
