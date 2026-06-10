package com.mojtaba.pocketledger.core.data.repository.contract

import kotlinx.coroutines.flow.Flow

/**
 * Base contract for repositories that expose local storage as the source of truth.
 *
 * Implementations must expose reactive reads as [Flow] streams backed by local persistence, keep
 * writes as suspend functions, and avoid blocking APIs. Remote sync is intentionally not required
 * by this contract. Repositories that do not have sync support yet should report
 * [SyncStatus.LOCAL_ONLY] instead of pretending remote state exists.
 *
 * List streams should be deterministic. DAO-backed repositories are expected to define stable
 * ordering in their queries so repeated collectors receive the same ordering for the same data.
 */
interface OfflineFirstRepository {
    /**
     * Stable diagnostic name for logs, sync dashboards, and tests.
     */
    val repositoryName: String

    /**
     * Observes the repository sync state.
     *
     * Local-only repositories should emit [SyncState.localOnly]. Future synced repositories can
     * surface pending local changes, active sync work, and sync errors through the same API without
     * changing feature-facing repository contracts.
     */
    fun observeSyncState(): Flow<SyncState>
}
