package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.repository.CacheRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cacheRepository: CacheRepository,
) : ViewModel() {

    val lastSyncTimestamp: StateFlow<Long> = cacheRepository.lastSyncTimestamp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun onClearCacheClicked() {
        viewModelScope.launch {
            cacheRepository.invalidateCache()
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // TODO: Implement logout logic, e.g., clearing user session
        }
    }
}
