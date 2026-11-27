package com.valoser.futacha.shared.watch

import com.valoser.futacha.shared.util.isWithinEpochInterval

fun WatchSnapshot.withReadAloudStatusUpdate(
    update: WatchReadAloudStatusUpdate,
    nowMillis: Long
): WatchSnapshot {
    val activeStatus = update.status?.takeIf { it.isFreshAt(nowMillis) }
    val updatedThreads = threads.map { thread ->
        val key = WatchThreadKey(
            boardId = thread.boardId,
            boardUrl = thread.boardUrl,
            threadId = thread.threadId
        )
        val currentStatus = thread.readAloudStatus
        val nextStatus = when {
            activeStatus?.matches(key) == true &&
                currentStatus != null &&
                currentStatus.updatedAtMillis > activeStatus.updatedAtMillis -> currentStatus
            activeStatus?.matches(key) == true -> activeStatus
            currentStatus != null &&
                update.updatedAtMillis > 0L &&
                currentStatus.updatedAtMillis > update.updatedAtMillis -> currentStatus
            else -> null
        }
        if (thread.readAloudStatus == nextStatus) {
            thread
        } else {
            thread.copy(readAloudStatus = nextStatus)
        }
    }
    return copy(threads = updatedThreads)
}

// Moved from WatchSnapshotBuilder.kt in Phase 3; public because the builder stays in :shared.
fun WatchReadAloudStatus.matches(key: WatchThreadKey): Boolean {
    return boardId == key.boardId &&
        boardUrl == key.boardUrl &&
        threadId == key.threadId
}

fun WatchReadAloudStatus.isFreshAt(nowMillis: Long): Boolean {
    return updatedAtMillis > 0 &&
        isWithinEpochInterval(nowMillis, updatedAtMillis, WATCH_READ_ALOUD_STATUS_MAX_AGE_MILLIS)
}
