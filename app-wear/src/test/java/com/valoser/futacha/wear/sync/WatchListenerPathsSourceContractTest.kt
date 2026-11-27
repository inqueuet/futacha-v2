package com.valoser.futacha.wear.sync

import com.valoser.futacha.shared.watch.WATCH_ALERT_PATH
import com.valoser.futacha.shared.watch.WATCH_COMMAND_PATH
import com.valoser.futacha.shared.watch.WATCH_PHONE_STATUS_PATH
import com.valoser.futacha.shared.watch.WATCH_READ_ALOUD_STATUS_PATH
import com.valoser.futacha.shared.watch.WATCH_REQUEST_SNAPSHOT_PATH
import com.valoser.futacha.shared.watch.WATCH_SNAPSHOT_ACK_PATH
import com.valoser.futacha.shared.watch.WATCH_SNAPSHOT_PATH
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** The listener wakes only for the paths the phone sends and the service handles (Phase 5, P5-1). */
class WatchListenerPathsSourceContractTest {
    private fun source(path: String) = listOf(File(path), File("app-wear/$path")).first { it.exists() }.readText()

    @Test
    fun theManifestListsExactlyThePathsTheServiceHandles() {
        val manifest = source("src/main/AndroidManifest.xml")
        val declared = Regex("""android:path="([^"]+)"""").findAll(manifest).map { it.groupValues[1] }.toSet()
        assertEquals(setOf(WATCH_SNAPSHOT_PATH, WATCH_READ_ALOUD_STATUS_PATH, WATCH_PHONE_STATUS_PATH, WATCH_ALERT_PATH), declared)
        assertEquals(false, manifest.contains("pathPrefix"))
        // Paths the watch writes itself must not wake it.
        listOf(WATCH_SNAPSHOT_ACK_PATH, WATCH_REQUEST_SNAPSHOT_PATH, WATCH_COMMAND_PATH).forEach {
            assertEquals(false, it in declared)
        }
        val service = source("src/main/java/com/valoser/futacha/wear/sync/WatchDataLayerListenerService.kt")
        listOf("WATCH_SNAPSHOT_PATH ->", "WATCH_READ_ALOUD_STATUS_PATH ->", "WATCH_PHONE_STATUS_PATH ->", "WATCH_ALERT_PATH ->")
            .forEach { assertEquals(it, true, service.contains(it)) }
    }
}
