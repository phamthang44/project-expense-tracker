package com.thang.projectexpensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thang.projectexpensetracker.data.AppDatabase
import com.thang.projectexpensetracker.infrastructure.ProjectRepository
import com.thang.projectexpensetracker.model.SyncStatus
import com.thang.projectexpensetracker.util.NetworkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Owns cloud-sync lifecycle:
 *   - Manual sync trigger (IDLE → SYNCING → SUCCESS | ERROR).
 *   - Auto-sync: observes Room Flows and uploads after a 3-second debounce.
 *   - Offline mode toggle.
 *   - Last sync timestamp and pending-item counter.
 */
class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(db.projectDao(), db.expenseDao())

    // ── State ──────────────────────────────────────────────────────────────

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(true)
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private var autoSyncJob: Job? = null
    private var periodicDownloadJob: Job? = null

    init {
        // Keep pending count in sync with Room data
        viewModelScope.launch {
            repository.getPendingCount().collectLatest { _pendingSyncCount.value = it }
        }
        // Auto-sync is enabled by default
        startAutoSyncObservation()
        // Periodically check real network connectivity
        startNetworkMonitoring()
        // Periodically pull changes from cloud (every 5 minutes)
        startPeriodicDownload()
    }

    // ── Network monitoring ──────────────────────────────────────────────────

    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            while (true) {
                val online = NetworkUtils.isOnline(getApplication())
                if (!online && !_isOffline.value) {
                    _isOffline.value = true
                    _syncStatus.value = SyncStatus.OFFLINE
                } else if (online && _isOffline.value) {
                    _isOffline.value = false
                    if (_syncStatus.value == SyncStatus.OFFLINE) {
                        _syncStatus.value = SyncStatus.IDLE
                    }
                }
                delay(5000) // Re-check every 5 seconds
            }
        }
    }

    // ── Periodic download (pull cloud changes from other apps) ──────────────

    /**
     * Periodically download expenses from Firestore to sync changes
     * made by other apps (e.g., React Native app).
     * Runs every 5 minutes when online and in the background.
     */
    private fun startPeriodicDownload() {
        periodicDownloadJob?.cancel()
        periodicDownloadJob = viewModelScope.launch {
            while (true) {
                delay(300000) // 5 minutes
                if (!_isOffline.value && 
                    _autoSyncEnabled.value &&
                    _syncStatus.value != SyncStatus.SYNCING) {
                    try {
                        repository.syncDownloadExpenses()
                    } catch (e: Exception) {
                        // Silently fail - this is background sync
                    }
                }
            }
        }
    }

    // ── Auto-sync ──────────────────────────────────────────────────────────

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun startAutoSyncObservation() {
        autoSyncJob?.cancel()
        autoSyncJob = viewModelScope.launch {
            repository.observeAllData()
                .drop(1)        // Skip the initial Room emission on app start
                .debounce(3000) // Wait 3 s for burst writes to settle
                .collectLatest {
                    if (_autoSyncEnabled.value &&
                        !_isOffline.value &&
                        _syncStatus.value != SyncStatus.SYNCING
                    ) {
                        performSync()
                    }
                }
        }
    }

    fun toggleAutoSync() {
        _autoSyncEnabled.value = !_autoSyncEnabled.value
        if (_autoSyncEnabled.value) startAutoSyncObservation()
        else autoSyncJob?.cancel()
    }

    // ── Manual sync ────────────────────────────────────────────────────────

    fun triggerSync() {
        viewModelScope.launch { performSync() }
    }

    /**
     * Pull only: download cloud changes without uploading local changes.
     * Useful to get the latest state from other apps without pushing local changes.
     */
    fun triggerPullOnly() {
        viewModelScope.launch { performPullOnly() }
    }

    private suspend fun performSync() {
        if (_syncStatus.value == SyncStatus.SYNCING) return
        _syncStatus.value = SyncStatus.SYNCING
        val success = repository.syncBidirectional()
        if (success) {
            _syncStatus.value   = SyncStatus.SUCCESS
            _lastSyncTime.value =
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            delay(3000)
            _syncStatus.value   = SyncStatus.IDLE
        } else {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    private suspend fun performPullOnly() {
        if (_syncStatus.value == SyncStatus.SYNCING) return
        _syncStatus.value = SyncStatus.SYNCING
        val success = repository.syncDownloadExpenses()
        if (success) {
            _syncStatus.value   = SyncStatus.SUCCESS
            _lastSyncTime.value =
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            delay(3000)
            _syncStatus.value   = SyncStatus.IDLE
        } else {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    // ── Offline / Error ────────────────────────────────────────────────────

    fun setOfflineMode(offline: Boolean) {
        _isOffline.value  = offline
        _syncStatus.value = if (offline) SyncStatus.OFFLINE else SyncStatus.IDLE
    }

    fun dismissSyncError() {
        _syncStatus.value = SyncStatus.IDLE
    }
}
