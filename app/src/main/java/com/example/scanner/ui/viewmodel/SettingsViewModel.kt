package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.SyncRepository
import com.example.scanner.data.sync.MasterDataSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val masterDataSynchronizer: MasterDataSynchronizer,
    private val syncRepository: SyncRepository
) : ViewModel() {

    // Directly use the StateFlow from MasterDataSynchronizer
    val lastSyncTimestamp: StateFlow<Long> = masterDataSynchronizer.lastSyncTimestampFlow

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onClearCacheClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // First upload offline logs
                val uploadedCount = syncRepository.uploadOfflineLogs()
                
                // Then fetch new master data
                masterDataSynchronizer.performSync()
                
                val message = if (uploadedCount > 0) {
                    "Synchronisation erfolgreich ($uploadedCount Scans hochgeladen)."
                } else {
                    "Stammdaten erfolgreich aktualisiert."
                }
                
                _uiEvent.emit(SettingsUiEvent.ShowMessage(message))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowError("Fehler bei der Synchronisation: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // TODO: Implement logout logic, e.g., clearing user session
        }
    }
}

sealed class SettingsUiEvent {
    data class ShowMessage(val message: String) : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
}
