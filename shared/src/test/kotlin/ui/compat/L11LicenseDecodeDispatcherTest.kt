package com.valoser.futacha.shared.ui.compat

import com.valoser.futacha.shared.util.SharedResourceFiles
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class L11LicenseDecodeDispatcherTest {
    // The app installs the asset reader in FutachaApplication; a host test does it here.
    @org.junit.Before fun installSharedResources() {
        SharedResourceFiles.install(org.robolectric.RuntimeEnvironment.getApplication())
    }

    @Test fun theActualLargeBundledNoticeIsDecodedOffTheCallerUiThreadWithoutChangingText() = runBlocking {
        val path = requireNotNull(FUTACHA_LICENSE_ASSETS.first { it.id == "voicevox-core-notices" }.resourcePath)
        val bytes = SharedResourceFiles.readBytes(path)
        assertTrue(bytes.size > 2_000_000, "This regression must cover the large bundled third-party notice")
        val expected = compatLicenseParagraphs(bytes)
        Executors.newSingleThreadExecutor { task -> Thread(task, "L11-test-ui") }.asCoroutineDispatcher().use { ui ->
            withContext(ui) {
                val caller = Thread.currentThread()
                val actual = loadCompatLicenseParagraphs(path, readBytes = { bytes }, decode = {
                    assertNotSame(caller, Thread.currentThread(), "The large decode/split must not occupy the caller UI thread")
                    compatLicenseParagraphs(it)
                })
                assertSame(caller, Thread.currentThread(), "The caller must resume on its UI dispatcher")
                assertEquals(expected, actual)
            }
        }
    }
}
