package com.valoser.futacha.shared.watch

/**
 * Encodes [snapshot] so its UTF-8 payload fits [maxBytes], trimming instead of
 * dropping the whole update: preview posts are removed first (oldest threads
 * first), then threads from the end, then boards. Returns null only when even
 * the empty snapshot does not fit.
 *
 * The trimming steps form a fixed sequence in which every step only removes content, so the
 * encoded size never grows along it. A binary search over the steps therefore finds the same
 * first fitting step as trying them in order, with about 8 encodes instead of up to ~120
 * (Phase 5, P5-2, candidate 4). The output is byte for byte the same.
 */
fun encodeWatchSnapshotWithinPayload(
    snapshot: WatchSnapshot,
    maxBytes: Int,
    encode: (WatchSnapshot) -> String
): String? {
    fun fits(candidate: WatchSnapshot): String? =
        encode(candidate).takeIf { it.encodeToByteArray().size <= maxBytes }

    fits(snapshot)?.let { return it }

    // Thread indexes whose previews are removed, in removal order (last thread first).
    val previewSteps = snapshot.threads.indices.reversed().filter { snapshot.threads[it].previewPosts.isNotEmpty() }
    val threadSteps = snapshot.threads.size
    val boardSteps = snapshot.boards.size
    val stepCount = previewSteps.size + threadSteps + boardSteps

    /** The snapshot after the first [step] trimming steps (step >= 1). */
    fun candidate(step: Int): WatchSnapshot {
        val previewsRemoved = minOf(step, previewSteps.size)
        val cleared = previewSteps.subList(0, previewsRemoved).toHashSet()
        var threads = snapshot.threads.mapIndexed { index, thread ->
            if (index in cleared) thread.copy(previewPosts = emptyList()) else thread
        }
        val threadsDropped = (step - previewSteps.size).coerceIn(0, threadSteps)
        threads = threads.dropLast(threadsDropped)
        val boardsDropped = (step - previewSteps.size - threadSteps).coerceIn(0, boardSteps)
        return snapshot.copy(
            threads = threads,
            unreadTotal = threads.sumOf { it.newReplyCount.toLong() }
                .coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            watchMatchTotal = threads.count { it.isWatchWordMatch },
            boards = snapshot.boards.dropLast(boardsDropped)
        )
    }

    var low = 1
    var high = stepCount
    var found: String? = null
    while (low <= high) {
        val mid = (low + high) ushr 1
        val encoded = fits(candidate(mid))
        if (encoded != null) {
            found = encoded
            high = mid - 1
        } else {
            low = mid + 1
        }
    }
    return found
}
