package com.thang.projectexpensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.model.SyncStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Owns the cloud-sync simulation exclusively:
 *   - Sync trigger / lifecycle (IDLE → SYNCING → SUCCESS | ERROR).
 *   - Offline mode toggle.
 *   - Last sync timestamp and pending-item counter.
 *
 * No database access, no project/expense state — pure sync concern.
 */
class SyncViewModel : ViewModel() {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    fun triggerSync() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            delay(2500) // Simulated network call

            val success = (1..10).random() <= 8
            if (success) {
                _syncStatus.value    = SyncStatus.SUCCESS
                _lastSyncTime.value  = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                _pendingSyncCount.value = 0
                delay(3000)
                _syncStatus.value    = SyncStatus.IDLE
            } else {
                _syncStatus.value    = SyncStatus.ERROR
            }
        }
    }

    fun setOfflineMode(offline: Boolean) {
        _isOffline.value  = offline
        _syncStatus.value = if (offline) SyncStatus.OFFLINE else SyncStatus.IDLE
    }

    fun dismissSyncError() {
        _syncStatus.value = SyncStatus.IDLE
    }
}
