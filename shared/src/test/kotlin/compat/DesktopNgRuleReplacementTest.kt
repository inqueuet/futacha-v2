package com.valoser.futacha.shared.compat

import com.valoser.futacha.shared.util.JvmFileSystem
import java.nio.file.Files
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class DesktopNgRuleReplacementTest {
    @Test fun replacementAndRejectedEditsSurviveReopening() = runBlocking {
        val root = Files.createTempDirectory("ng-replacement").toFile()
        var store = androidCompatibilityStore(JvmFileSystem(root))
        try {
            store.initialize()
            val expected = verifyCompatNgReplacementPersistence(store)
            store.close()
            store = androidCompatibilityStore(JvmFileSystem(root))
            store.initialize()
            assertEquals(expected, store.ngRules.first())
        } finally { store.close(); root.deleteRecursively() }
    }
}
