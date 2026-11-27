package com.valoser.futacha.shared.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The files bundled with the shared module (licenses, the tutorial image), packaged as Android assets under
 * [ASSET_ROOT]. Replaces Compose Multiplatform's `Res.readBytes` (Phase 2, docs/v2-phase2-plan.md); the paths are
 * the same ("files/…").
 */
object SharedResourceFiles {
    internal const val ASSET_ROOT = "futacha-shared/"

    @Volatile
    private var appContext: Context? = null

    /** Called first in Application.onCreate (and by host tests). */
    fun install(context: Context) {
        appContext = context.applicationContext ?: context
    }

    suspend fun readBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
        val context = checkNotNull(appContext) { "SharedResourceFiles.install was not called" }
        context.assets.open(ASSET_ROOT + path).use { it.readBytes() }
    }
}
