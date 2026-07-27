package com.mojtaba.folentra.core.data.repository.contract

/**
 * Lightweight vocabulary for future pending local change tracking.
 *
 * This does not imply that a sync queue exists today. It documents the write shapes future sync
 * work will need to represent while current repositories remain honest local-only implementations.
 */
enum class LocalChange {
    INSERT,
    UPDATE,
    DELETE,
    LINK,
    UNLINK,
}
