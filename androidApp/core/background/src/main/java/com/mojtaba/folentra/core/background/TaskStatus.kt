package com.mojtaba.folentra.core.background

enum class TaskStatus {
    NotScheduled,
    Enqueued,
    Running,
    Succeeded,
    Failed,
    Cancelled,
    Blocked,
    Unknown,
}
