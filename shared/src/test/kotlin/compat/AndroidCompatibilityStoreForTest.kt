package com.valoser.futacha.shared.compat

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.valoser.futacha.compat.AndroidCompatibilityStore
import com.valoser.futacha.shared.util.FileSystem
import kotlinx.coroutines.flow.first

/**
 * The real Android store, as the tests' replacement for the removed desktop store.
 * One database per file-system root, so a second store on the same root reopens
 * the same data (the tests' "restart"). Robolectric gives every test a fresh app.
 */
fun androidCompatibilityStore(
    fileSystem: FileSystem,
    nowMillis: () -> Long = System::currentTimeMillis
): AndroidCompatibilityStore {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val root = fileSystem.resolveAbsolutePath("")
    val name = "compat-" + root.hashCode().toUInt().toString(16) + ".db"
    return AndroidCompatibilityStore(context, fileSystem, name, nowMillis)
}

/** The desktop store's synchronous close(), for the tests that reopen a store. */
fun AndroidCompatibilityStore.close() = kotlinx.coroutines.runBlocking { closeForTest() }

/**
 * Registers a board for a test that writes tabs or history directly. The Android
 * store enforces the board foreign key; the app always imports the board first
 * (FutachaThreadFeatures / CompatibilityApp), the desktop store did not check.
 */
suspend fun CompatibilityStore.registerTestBoard(key: String, name: String = key) {
    if (boards.first().any { it.key == key }) return
    val url = "https://may.2chan.net/$key/"
    upsertBoard(CompatBoard(key, name, url, url, boards.first().size))
}
