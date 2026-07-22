package com.mojtaba.folentra.core.data.repository.contract

/**
 * Coarse sync lifecycle state for offline-first repositories.
 */
enum class SyncStatus {
    /**
     * The repository is intentionally backed only by local storage.
     */
    LOCAL_ONLY,

    /**
     * Remote sync is configured and there is no known pending work.
     */
    IDLE,

    /**
     * Sync work is currently reading or writing remote state.
     */
    SYNCING,

    /**
     * Local writes are waiting to be pushed when sync support is available.
     */
    PENDING_CHANGES,

    /**
     * The most recent sync attempt failed.
     */
    ERROR,
}
