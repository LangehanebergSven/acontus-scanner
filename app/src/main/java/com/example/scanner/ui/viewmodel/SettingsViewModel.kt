package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.sync.MasterDataSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val masterDataSynchronizer: MasterDataSynchronizer
) : ViewModel() {

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    fun onClearCacheClicked() {
        viewModelScope.launch {
            masterDataSynchronizer.performSync()
            // In a real app, we would update the timestamp here or observe it from prefs
            _lastSyncTimestamp.value = System.currentTimeMillis()
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // TODO: Implement logout logic, e.g., clearing user session
        }
    }
}
