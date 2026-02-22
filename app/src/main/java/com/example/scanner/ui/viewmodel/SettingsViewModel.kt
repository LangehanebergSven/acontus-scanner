package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.CacheRepository
import com.example.scanner.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cacheRepository: CacheRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    fun onClearCacheClicked() {
        viewModelScope.launch {
            cacheRepository.invalidateCache()
        }
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            syncRepository.synchronizePendingQueries()
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // TODO: Implement logout logic, e.g., clearing user session
        }
    }
}
