package com.mojtaba.folentra.core.data.repository.contract

/**
 * Observable sync metadata for an offline-first repository.
 *
 * The current data layer is local-only, so local repository implementations should emit
 * [localOnly]. When remote sync is introduced, this type can represent pending writes, active sync,
 * last successful sync time, and sync errors without changing read or write repository APIs.
 */
data class SyncState(
    val status: SyncStatus,
    val pendingLocalChanges: Int = 0,
    val lastSyncedAt: Long? = null,
    val errorMessage: String? = null,
) {
    companion object {
        fun localOnly(): SyncState = SyncState(status = SyncStatus.LOCAL_ONLY)
    }
}
