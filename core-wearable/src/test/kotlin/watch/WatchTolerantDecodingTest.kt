package com.valoser.futacha.shared.watch

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatchTolerantDecodingTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val status = """{"boardId":"b","boardUrl":"https://may.2chan.net/b/","threadId":"1","state":"%s","postId":null,""" +
        """"currentIndex":0,"totalPosts":3,"updatedAtMillis":5}"""
    private fun snapshot(state: String) =
        """{"generatedAtMillis":10,"boards":[],"threads":[{"threadId":"1","boardId":"b","boardName":"b",""" +
            """"boardUrl":"https://may.2chan.net/b/","title":"t","thumbnailUrl":null,"replyCount":3,"previousReplyCount":null,""" +
            """"newReplyCount":1,"lastVisitedEpochMillis":0,"isWatchWordMatch":false,"previewPosts":[],""" +
            """"readAloudStatus":${status.format(state)}}],"watchWords":[],"unreadTotal":1,"watchMatchTotal":0}"""

    @Test
    fun aKnownSnapshotDecodesExactlyAsTheStrictDecoder() {
        val encoded = snapshot("Paused")
        assertEquals(json.decodeFromString(WatchSnapshot.serializer(), encoded), decodeWatchSnapshotTolerant(json, encoded))
    }

    @Test
    fun anUnknownStateDropsOnlyThatReadAloudStatus() {
        val decoded = decodeWatchSnapshotTolerant(json, snapshot("Stopped"))
        assertNull(decoded.threads.single().readAloudStatus)
        assertEquals("t", decoded.threads.single().title)
        assertTrue(decoded.hasValidTransportShape())
    }

    @Test
    fun otherBrokenSnapshotsStillFail() {
        assertFailsWith<SerializationException> {
            decodeWatchSnapshotTolerant(json, snapshot("Paused").replace(""","watchMatchTotal":0""", ""))
        }
        assertFailsWith<SerializationException> { decodeWatchSnapshotTolerant(json, "{broken") }
    }
}
