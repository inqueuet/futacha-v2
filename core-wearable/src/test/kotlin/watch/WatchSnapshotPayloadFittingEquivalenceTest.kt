package com.valoser.futacha.shared.watch

import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The binary search must give byte-for-byte the output of the previous step-by-step fitting
 * (Phase 5, P5-2, candidate 4). The previous code is kept here as the reference.
 */
class WatchSnapshotPayloadFittingEquivalenceTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun referenceFitting(snapshot: WatchSnapshot, maxBytes: Int, encode: (WatchSnapshot) -> String): String? {
        fun fits(candidate: WatchSnapshot): String? = encode(candidate).takeIf { it.encodeToByteArray().size <= maxBytes }
        fits(snapshot)?.let { return it }
        var threads = snapshot.threads
        fun current(): WatchSnapshot = snapshot.copy(
            threads = threads,
            unreadTotal = threads.sumOf { it.newReplyCount.toLong() }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            watchMatchTotal = threads.count { it.isWatchWordMatch }
        )
        for (index in threads.indices.reversed()) {
            if (threads[index].previewPosts.isEmpty()) continue
            threads = threads.toMutableList().also { it[index] = it[index].copy(previewPosts = emptyList()) }
            fits(current())?.let { return it }
        }
        while (threads.isNotEmpty()) {
            threads = threads.dropLast(1)
            fits(current())?.let { return it }
        }
        var boards = snapshot.boards
        while (boards.isNotEmpty()) {
            boards = boards.dropLast(1)
            fits(current().copy(boards = boards))?.let { return it }
        }
        return null
    }

    private fun randomSnapshot(random: Random): WatchSnapshot {
        val threads = List(random.nextInt(0, 21)) { i ->
            WatchThreadSummary(
                "$i", "b", "B", "https://may.2chan.net/b/", "題".repeat(random.nextInt(1, 60)),
                if (random.nextBoolean()) null else "https://may.2chan.net/b/thumb/$i.jpg",
                random.nextInt(0, 1001), null, random.nextInt(0, 1001), 0L, random.nextBoolean(),
                List(random.nextInt(0, 6)) { p -> WatchPostPreview("$p", "本".repeat(random.nextInt(0, 400)), null) }
            )
        }
        return WatchSnapshot(
            1L,
            List(random.nextInt(0, 81)) { WatchBoard("b$it", "板$it", "カテゴリ", "https://may.2chan.net/b$it/", false) },
            threads,
            List(random.nextInt(0, 10)) { "語$it" },
            threads.sumOf { it.newReplyCount },
            threads.count { it.isWatchWordMatch }
        )
    }

    @Test
    fun givesTheSameBytesAsTheStepByStepFittingOnManyInputs() {
        val random = Random(20261126)
        var oldEncodes = 0L
        var newEncodes = 0L
        repeat(400) {
            val snapshot = randomSnapshot(random)
            val full = json.encodeToString(WatchSnapshot.serializer(), snapshot).encodeToByteArray().size
            val limits = listOf(10, 200, 1024, full / 4, full / 2, full - 1, full, 60 * 1024, 96 * 1024, random.nextInt(1, full + 2))
            for (limit in limits) {
                val expected = referenceFitting(snapshot, limit) { oldEncodes++; json.encodeToString(WatchSnapshot.serializer(), it) }
                val actual = encodeWatchSnapshotWithinPayload(snapshot, limit) { newEncodes++; json.encodeToString(WatchSnapshot.serializer(), it) }
                assertEquals(expected, actual, "limit=$limit full=$full")
            }
        }
        println("PAYLOAD_FITTING_ENCODES old=$oldEncodes new=$newEncodes")
        assertTrue(newEncodes < oldEncodes)
    }
}
