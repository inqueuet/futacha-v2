package com.valoser.futacha.shared.mix

import com.valoser.futacha.compat.AndroidCompatibilityStore

import com.valoser.futacha.shared.compat.androidCompatibilityStore
import com.valoser.futacha.shared.compat.close

import com.valoser.futacha.shared.util.JvmFileSystem
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class MixBackupRoundTripTest {
    private fun <T> withStore(block: suspend (AndroidCompatibilityStore) -> T): T = runBlocking {
        val root = Files.createTempDirectory("mix-backup").toFile()
        val store = androidCompatibilityStore(JvmFileSystem(root))
        try { store.initialize(); block(store) } finally { store.close(); root.deleteRecursively() }
    }

    private suspend fun AndroidCompatibilityStore.names(): List<String> =
        (assertIs<MixLoadResult.Ok>(MixStore(this).load())).mixes.map { it.name }

    @Test fun theExportCarriesTheMixesAndARestoreReplacesThem() {
        val payload = withStore { s ->
            MixStore(s).create("A", listOf("https://may.2chan.net/b/"), MixOrderKind.ALTERNATE, 1)
            s.exportSettingsBackup()
        }
        assertTrue(MIX_DEFINITIONS_KEY in payload)
        withStore { t ->
            MixStore(t).create("Old", listOf("https://img.2chan.net/b/"), MixOrderKind.NEW, 2)
            t.importSettingsBackup(payload, restoreUserSettings = true, restoreNgRules = false)
            assertEquals(listOf("A"), t.names())
        }
    }

    @Test fun aBackupWithoutMixesKeepsTheCurrentOnes() {
        val payload = withStore { it.exportSettingsBackup() }
        withStore { t ->
            MixStore(t).create("Keep", listOf("https://may.2chan.net/b/"), MixOrderKind.NEW, 1)
            t.importSettingsBackup(payload, restoreUserSettings = true, restoreNgRules = false)
            assertEquals(listOf("Keep"), t.names())
        }
    }
}
