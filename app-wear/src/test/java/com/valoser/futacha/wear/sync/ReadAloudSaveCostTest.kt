package com.valoser.futacha.wear.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.valoser.futacha.shared.watch.WatchBoard
import com.valoser.futacha.shared.watch.WatchPostPreview
import com.valoser.futacha.shared.watch.WatchReadAloudPlaybackState
import com.valoser.futacha.shared.watch.WatchReadAloudStatus
import com.valoser.futacha.shared.watch.WatchReadAloudStatusUpdate
import com.valoser.futacha.shared.watch.WatchSnapshot
import com.valoser.futacha.shared.watch.WatchThreadSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Counts what ten read-aloud updates (one every 5 s on the phone) write to the watch's storage
 * (Phase 5, P5-1, candidate 2), and checks the result survives a restart of the watch app.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReadAloudSaveCostTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs get() = context.getSharedPreferences("futacha_watch_snapshot", Context.MODE_PRIVATE)

    private fun resetMemory() {
        val field = WatchSnapshotStore::class.java.getDeclaredField("snapshotState").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(WatchSnapshotStore) as MutableStateFlow<WatchSnapshot?>).value = null
    }

    @Before
    fun reset() {
        resetMemory()
        prefs.edit().clear().commit()
    }

    /** A full-size snapshot: 20 threads with 5 long previews each. */
    private fun bigSnapshot(now: Long): WatchSnapshot {
        val threads = (1..20).map { i ->
            WatchThreadSummary(
                "$i", "may-b", "may/b", "https://may.2chan.net/b/", "スレ$i".repeat(10), null, 100, 90, 10, now,
                false, (1..5).map { p -> WatchPostPreview("$p", "本文".repeat(100), "26/11/26") }
            )
        }
        return WatchSnapshot(
            now - 10_000L, listOf(WatchBoard("may-b", "may/b", "", "https://may.2chan.net/b/", false)),
            threads, emptyList(), 200, 0
        )
    }

    @Test
    fun tenReadAloudUpdatesWriteLittleAndSurviveARestart() = runBlocking {
        val now = System.currentTimeMillis()
        WatchSnapshotStore.save(context, bigSnapshot(now))
        var writes = 0
        var bytes = 0L
        // Each changed value is a write of that value (SharedPreferences rewrites the whole file on commit,
        // so this undercounts the file size equally before and after).
        var previous = prefs.all.mapValues { it.value.toString() }
        var last: WatchReadAloudStatus? = null
        for (i in 1..10) {
            last = WatchReadAloudStatus(
                "may-b", "https://may.2chan.net/b/", "3",
                if (i == 10) WatchReadAloudPlaybackState.Paused else WatchReadAloudPlaybackState.Speaking,
                "$i", i, 100, now + i
            )
            WatchSnapshotStore.applyReadAloudStatusUpdate(
                context, WatchReadAloudStatusUpdate(last, now + i, sessionId = 7L, sequence = i.toLong())
            )
            val current = prefs.all.mapValues { it.value.toString() }
            val changed = current.filter { (k, v) -> previous[k] != v }
            if (changed.isNotEmpty() || current.keys != previous.keys) writes++
            bytes += changed.values.sumOf { it.encodeToByteArray().size.toLong() }
            previous = current
        }
        println("READ_ALOUD_SAVE_COST writes=$writes bytes=$bytes")
        val expected = WatchSnapshotStore.observe().value
        assertEquals(last, expected!!.threads.first { it.threadId == "3" }.readAloudStatus)

        resetMemory() // the watch app process restarts
        assertEquals(expected, WatchSnapshotStore.getSnapshot(context))
    }

    @Test
    fun aNewFullSnapshotClearsTheSavedStatusesAndAStaleOverlayIsIgnored() = runBlocking {
        val now = System.currentTimeMillis()
        val base = bigSnapshot(now)
        WatchSnapshotStore.save(context, base)
        val status = WatchReadAloudStatus("may-b", "https://may.2chan.net/b/", "3", WatchReadAloudPlaybackState.Speaking, "1", 1, 100, now)
        WatchSnapshotStore.applyReadAloudStatusUpdate(context, WatchReadAloudStatusUpdate(status, now, 9L, 1L))
        assertEquals(true, prefs.contains("read_aloud_overlay_json"))

        val next = base.copy(generatedAtMillis = base.generatedAtMillis + 1)
        WatchSnapshotStore.save(context, next)
        assertEquals(false, prefs.contains("read_aloud_overlay_json"))
        assertEquals(false, prefs.contains("read_aloud_overlay_generated_at"))

        // An overlay left for another snapshot must not be laid over this one.
        prefs.edit().putString("read_aloud_overlay_json", "[null]").putLong("read_aloud_overlay_generated_at", 1L).commit()
        resetMemory()
        assertEquals(next, WatchSnapshotStore.getSnapshot(context))
    }
}
