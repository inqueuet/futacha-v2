package com.valoser.futacha.wear.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.valoser.futacha.shared.watch.WatchBoard
import com.valoser.futacha.shared.watch.WatchSnapshot
import com.valoser.futacha.shared.watch.WatchThreadSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pins how the released watch stores snapshots (Phase 5, P5-0) before P5-1 changes the saved form:
 * the preference file and key, the stored JSON, "same content → no save (no ack)", and older snapshots ignored.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WatchSnapshotStoreRobolectricTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val json = Json { ignoreUnknownKeys = true }

    private fun snapshot(generatedAt: Long, title: String = "スレ") = WatchSnapshot(
        generatedAtMillis = generatedAt,
        boards = listOf(WatchBoard("may-b", "may/b", "二次元", "https://may.2chan.net/b/", false)),
        threads = listOf(
            WatchThreadSummary(
                "123", "may-b", "may/b", "https://may.2chan.net/b/", title, null,
                10, 8, 2, 0L, false, emptyList()
            )
        ),
        watchWords = emptyList(),
        unreadTotal = 2,
        watchMatchTotal = 0
    )

    private val prefs get() = context.getSharedPreferences("futacha_watch_snapshot", Context.MODE_PRIVATE)

    @Before
    fun reset() {
        // The store is a process-wide object; start every test without a snapshot in memory.
        val field = WatchSnapshotStore::class.java.getDeclaredField("snapshotState").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(WatchSnapshotStore) as MutableStateFlow<WatchSnapshot?>).value = null
        prefs.edit().clear().commit()
    }

    @Test
    fun aSavedSnapshotIsWrittenUnderTheReleasedKeyAsItsJson() = runBlocking {
        val s = snapshot(System.currentTimeMillis() - 1_000L)
        assertTrue(WatchSnapshotStore.save(context, s))
        val stored = prefs.getString("snapshot_json", null)
        assertEquals(json.encodeToString(WatchSnapshot.serializer(), s), stored)
        assertEquals(s, WatchSnapshotStore.observe().value)
    }

    @Test
    fun theSameContentIsNotSavedAgainSoNoAckIsSent() = runBlocking {
        val s = snapshot(System.currentTimeMillis() - 1_000L)
        assertTrue(WatchSnapshotStore.save(context, s))
        assertFalse(WatchSnapshotStore.save(context, s))
    }

    @Test
    fun anOlderSnapshotDoesNotReplaceANewerOne() = runBlocking {
        val now = System.currentTimeMillis()
        val newer = snapshot(now - 1_000L, "新しい")
        assertTrue(WatchSnapshotStore.save(context, newer))
        assertFalse(WatchSnapshotStore.save(context, snapshot(now - 60_000L, "古い")))
        assertEquals(newer, WatchSnapshotStore.observe().value)
    }

    @Test
    fun aSnapshotStoredByTheReleasedWatchIsReadBack() = runBlocking {
        // What an already-installed watch has on disk; an updated watch must still read it.
        val s = snapshot(System.currentTimeMillis() - 1_000L)
        prefs.edit().putString("snapshot_json", json.encodeToString(WatchSnapshot.serializer(), s)).commit()
        assertEquals(s, WatchSnapshotStore.getSnapshot(context))
    }

    @Test
    fun aBrokenOrBadlyShapedStoredSnapshotIsIgnored() = runBlocking {
        prefs.edit().putString("snapshot_json", "{broken").commit()
        assertNull(WatchSnapshotStore.getSnapshot(context))
        val badTotals = snapshot(System.currentTimeMillis()).copy(unreadTotal = 99)
        assertNull(WatchSnapshotStore.decodeSnapshot(json.encodeToString(WatchSnapshot.serializer(), badTotals)))
    }
}
