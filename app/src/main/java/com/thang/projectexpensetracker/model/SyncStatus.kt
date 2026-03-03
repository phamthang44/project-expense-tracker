package com.thang.projectexpensetracker.model

/**
 * Represents the current state of the cloud synchronisation operation.
 *
 * Single Responsibility: pure domain enum for sync state —
 * decoupled from ViewModel lifecycle and UI concerns.
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE
}
