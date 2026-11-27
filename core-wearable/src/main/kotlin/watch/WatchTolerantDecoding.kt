package com.valoser.futacha.shared.watch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Decodes a snapshot on the watch, dropping only a thread's read-aloud status whose state this
 * watch does not know (a newer phone), instead of losing the whole snapshot (Phase 5, decision 5).
 * A snapshot without unknown values decodes exactly as before.
 */
fun decodeWatchSnapshotTolerant(json: Json, encoded: String): WatchSnapshot {
    val strict = runCatching { json.decodeFromString(WatchSnapshot.serializer(), encoded) }
    strict.getOrNull()?.let { return it }
    val root = runCatching { json.parseToJsonElement(encoded) as? JsonObject }.getOrNull()
        ?: return strict.getOrThrow()
    val threads = root["threads"] as? JsonArray ?: return strict.getOrThrow()
    var changed = false
    val cleaned = JsonArray(threads.map { thread ->
        val obj = thread as? JsonObject ?: return@map thread
        val status = obj["readAloudStatus"] as? JsonObject ?: return@map thread
        val state = (status["state"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (state != null && state in KNOWN_PLAYBACK_STATES) return@map thread
        changed = true
        JsonObject(obj - "readAloudStatus")
    })
    if (!changed) return strict.getOrThrow()
    return json.decodeFromJsonElement(WatchSnapshot.serializer(), JsonObject(root + ("threads" to cleaned)))
}

private val KNOWN_PLAYBACK_STATES = WatchReadAloudPlaybackState.entries.map { it.name }.toSet()
