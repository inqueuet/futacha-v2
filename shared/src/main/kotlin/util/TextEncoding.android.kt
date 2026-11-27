@file:kotlin.OptIn(kotlin.ExperimentalMultiplatform::class)

package com.valoser.futacha.shared.util

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/**
 * Provides access to platform-specific text encoding utilities.
 * We specifically need Shift_JIS for Futaba form submissions.
 */
object TextEncoding {
    // On Android "Shift_JIS" and "windows-31j" are the same ICU converter (CP932, checked on API 35). Asking for
    // windows-31j first keeps that meaning where "Shift_JIS" is plain JIS (a host JVM, as in Robolectric).
    private val shiftJis: Charset = runCatching { Charset.forName("windows-31j") }
        .getOrElse { Charset.forName("Shift_JIS") }

    fun encodeToShiftJis(text: String): ByteArray {
        // Replace unmappable characters so Futaba accepts the payload instead of failing.
        return canonicalCp932Text(text)
            .toByteArray(shiftJis)
    }

    fun decodeToString(bytes: ByteArray, contentType: String? = null): String {
        return when {
            contentType?.contains("shift_jis", ignoreCase = true) == true -> String(bytes, shiftJis)
            contentType?.contains("shift-jis", ignoreCase = true) == true -> String(bytes, shiftJis)
            // With an explicit UTF-8 header, a character cut off by a head-only/Range read
            // must not push the whole body through the Shift_JIS fallback.
            contentType?.contains("utf-8", ignoreCase = true) == true ->
                decodeUtf8Strict(bytes)
                    ?: decodeUtf8WithTruncatedTail(bytes) { decodeUtf8Strict(it) }
                    ?: String(bytes, shiftJis)
            else -> decodeUtf8Strict(bytes) ?: String(bytes, shiftJis)
        }
    }

    private fun decodeUtf8Strict(bytes: ByteArray): String? {
        if (!isValidUtf8Bytes(bytes)) return null
        return runCatching {
            Charsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        }.getOrNull()
    }
}
