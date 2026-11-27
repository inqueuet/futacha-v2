package com.valoser.futacha.wear.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageEvent
import com.valoser.futacha.shared.watch.WATCH_SNAPSHOT_KEY
import com.valoser.futacha.shared.watch.WATCH_SNAPSHOT_PATH
import com.valoser.futacha.shared.watch.WatchBoard
import com.valoser.futacha.shared.watch.WatchSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pins which message forms the released watch accepts on the snapshot path (Phase 5, P5-0). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WatchDataLayerListenerRobolectricTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val json = Json { ignoreUnknownKeys = true }
    private val service = Robolectric.setupService(WatchDataLayerListenerService::class.java)

    private fun snapshot(generatedAt: Long) = WatchSnapshot(
        generatedAt, listOf(WatchBoard("may-b", "may/b", "", "https://may.2chan.net/b/", false)),
        emptyList(), emptyList(), 0, 0
    )

    private fun message(path: String, data: ByteArray) = object : MessageEvent {
        override fun getRequestId() = 1
        override fun getPath() = path
        override fun getData() = data
        override fun getSourceNodeId() = "phone"
    }

    private fun stored(): WatchSnapshot? {
        repeat(100) {
            WatchSnapshotStore.observe().value?.let { return it }
            Thread.sleep(20)
        }
        return null
    }

    @Before
    fun reset() {
        val field = WatchSnapshotStore::class.java.getDeclaredField("snapshotState").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(WatchSnapshotStore) as MutableStateFlow<WatchSnapshot?>).value = null
        context.getSharedPreferences("futacha_watch_snapshot", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun aRawJsonMessageFromAnOlderPhoneIsSaved() {
        val s = snapshot(System.currentTimeMillis() - 1_000L)
        service.onMessageReceived(message(WATCH_SNAPSHOT_PATH, json.encodeToString(WatchSnapshot.serializer(), s).encodeToByteArray()))
        assertEquals(s, stored())
    }

    @Test
    fun aDataMapMessageIsSaved() {
        val s = snapshot(System.currentTimeMillis() - 1_000L)
        val map = DataMap().apply { putString(WATCH_SNAPSHOT_KEY, json.encodeToString(WatchSnapshot.serializer(), s)) }
        service.onMessageReceived(message(WATCH_SNAPSHOT_PATH, map.toByteArray()))
        assertEquals(s, stored())
    }

    @Test
    fun otherPathsAndEmptyOrBrokenPayloadsSaveNothing() {
        val encoded = json.encodeToString(WatchSnapshot.serializer(), snapshot(System.currentTimeMillis())).encodeToByteArray()
        service.onMessageReceived(message("/futacha/unknown", encoded))
        service.onMessageReceived(message(WATCH_SNAPSHOT_PATH, ByteArray(0)))
        service.onMessageReceived(message(WATCH_SNAPSHOT_PATH, "{broken".encodeToByteArray()))
        Thread.sleep(300)
        assertNull(WatchSnapshotStore.observe().value)
    }
}
