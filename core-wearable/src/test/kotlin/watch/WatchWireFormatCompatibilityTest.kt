package com.valoser.futacha.shared.watch

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Pins the phone ↔ watch JSON as it is sent today (Phase 5, P5-0). A watch and a phone are
 * updated separately, so a released watch decodes whatever the phone sends and the reverse:
 * these samples must keep decoding, and the phone must keep producing exactly them.
 */
class WatchWireFormatCompatibilityTest {
    /** The configuration every sender and receiver uses (phone WatchSyncManager, watch store/listener/client). */
    private val json = Json { ignoreUnknownKeys = true }

    private val status = WatchReadAloudStatus(
        boardId = "may-b",
        boardUrl = "https://may.2chan.net/b/",
        threadId = "123",
        state = WatchReadAloudPlaybackState.Speaking,
        postId = "130",
        currentIndex = 7,
        totalPosts = 42,
        updatedAtMillis = 1_700_000_000_500L
    )

    private val snapshot = WatchSnapshot(
        generatedAtMillis = 1_700_000_000_000L,
        boards = listOf(WatchBoard("may-b", "may/b", "二次元", "https://may.2chan.net/b/", true)),
        threads = listOf(
            WatchThreadSummary(
                threadId = "123",
                boardId = "may-b",
                boardName = "may/b",
                boardUrl = "https://may.2chan.net/b/",
                title = "スレ",
                thumbnailUrl = null,
                replyCount = 42,
                previousReplyCount = 40,
                newReplyCount = 2,
                lastVisitedEpochMillis = 1_699_999_999_000L,
                isWatchWordMatch = true,
                previewPosts = listOf(WatchPostPreview("124", "本文", null)),
                readAloudStatus = status
            ),
            WatchThreadSummary(
                threadId = "200",
                boardId = "may-b",
                boardName = "may/b",
                boardUrl = "https://may.2chan.net/b/",
                title = "二つ目",
                thumbnailUrl = "https://may.2chan.net/b/thumb/1s.jpg",
                replyCount = 3,
                previousReplyCount = null,
                newReplyCount = 0,
                lastVisitedEpochMillis = 0L,
                isWatchWordMatch = false,
                previewPosts = emptyList()
            )
        ),
        watchWords = listOf("ねこ"),
        unreadTotal = 2,
        watchMatchTotal = 1
    )

    private val statusJson =
        """{"boardId":"may-b","boardUrl":"https://may.2chan.net/b/","threadId":"123","state":"Speaking",""" +
            """"postId":"130","currentIndex":7,"totalPosts":42,"updatedAtMillis":1700000000500}"""

    private val snapshotJson =
        """{"generatedAtMillis":1700000000000,""" +
            """"boards":[{"id":"may-b","name":"may/b","category":"二次元","url":"https://may.2chan.net/b/","pinned":true}],""" +
            """"threads":[{"threadId":"123","boardId":"may-b","boardName":"may/b","boardUrl":"https://may.2chan.net/b/",""" +
            """"title":"スレ","thumbnailUrl":null,"replyCount":42,"previousReplyCount":40,"newReplyCount":2,""" +
            """"lastVisitedEpochMillis":1699999999000,"isWatchWordMatch":true,""" +
            """"previewPosts":[{"postId":"124","text":"本文","postedAtText":null}],""" +
            """"readAloudStatus":$statusJson},""" +
            """{"threadId":"200","boardId":"may-b","boardName":"may/b","boardUrl":"https://may.2chan.net/b/",""" +
            """"title":"二つ目","thumbnailUrl":"https://may.2chan.net/b/thumb/1s.jpg","replyCount":3,""" +
            """"previousReplyCount":null,"newReplyCount":0,"lastVisitedEpochMillis":0,"isWatchWordMatch":false,""" +
            """"previewPosts":[]}],""" +
            """"watchWords":["ねこ"],"unreadTotal":2,"watchMatchTotal":1}"""

    @Test
    fun theSnapshotIsSentInExactlyTheReleasedShape() {
        assertEquals(snapshotJson, json.encodeToString(WatchSnapshot.serializer(), snapshot))
    }

    @Test
    fun theReleasedSnapshotDecodesAndPassesTheWatchShapeCheck() {
        val decoded = json.decodeFromString(WatchSnapshot.serializer(), snapshotJson)
        assertEquals(snapshot, decoded)
        assertTrue(decoded.hasValidTransportShape())
    }

    @Test
    fun theRawGeneratedAtKeyStaysTheSame() {
        // Read by name (not through the serializer) on the Data Layer side.
        assertTrue(snapshotJson.startsWith("""{"generatedAtMillis":"""))
        assertEquals(1_700_000_000_000L, json.parseToJsonElement(snapshotJson).let {
            (it as kotlinx.serialization.json.JsonObject)["generatedAtMillis"].toString().toLong()
        })
    }

    @Test
    fun aThreadFromAPhoneWithoutReadAloudStatusStillDecodes() {
        val old = snapshotJson.replace(""","readAloudStatus":$statusJson""", "")
        val decoded = json.decodeFromString(WatchSnapshot.serializer(), old)
        assertEquals(null, decoded.threads.first().readAloudStatus)
    }

    @Test
    fun unknownFieldsFromANewerSenderAreIgnored() {
        val newer = snapshotJson.replace("""{"generatedAtMillis":""", """{"futureField":[1,2],"generatedAtMillis":""")
        assertEquals(snapshot, json.decodeFromString(WatchSnapshot.serializer(), newer))
    }

    @Test
    fun aMissingRequiredFieldFailsTheWholeSnapshot() {
        // Characterization: removing or renaming a field without a default breaks every released watch.
        val missing = snapshotJson.replace(""","watchMatchTotal":1""", "")
        assertFailsWith<SerializationException> { json.decodeFromString(WatchSnapshot.serializer(), missing) }
    }

    @Test
    fun anUnknownPlaybackStateFailsTheWholeSnapshotOnTheReleasedWatch() {
        // Characterization of the released watch: a new enum value from the phone drops the whole snapshot.
        // The phone must not add values (Phase 5 decision 5).
        val unknown = snapshotJson.replace(""""state":"Speaking"""", """"state":"Stopped"""")
        assertFailsWith<SerializationException> { json.decodeFromString(WatchSnapshot.serializer(), unknown) }
    }

    @Test
    fun anUnknownCommandTypeFailsToDecodeOnTheReleasedPhone() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(WatchCommand.serializer(), """{"type":"FutureCommand","commandId":"c"}""")
        }
    }

    @Test
    fun commandsKeepTheirShape() {
        val command = WatchCommand(WatchCommandType.OpenThreadOnPhone, "may-b", "https://may.2chan.net/b/", "123", "c1")
        val encoded = """{"type":"OpenThreadOnPhone","boardId":"may-b","boardUrl":"https://may.2chan.net/b/","threadId":"123","commandId":"c1"}"""
        assertEquals(encoded, json.encodeToString(WatchCommand.serializer(), command))
        assertEquals(command, json.decodeFromString(WatchCommand.serializer(), encoded))
        assertEquals(WatchCommand(WatchCommandType.Refresh), json.decodeFromString(WatchCommand.serializer(), """{"type":"Refresh"}"""))
        assertEquals(
            listOf(
                "OpenThreadOnPhone", "Refresh", "SelectBoard", "StartReadAloudOnPhone", "PauseReadAloudOnPhone",
                "StopReadAloudOnPhone", "NextReadAloudOnPhone", "PreviousReadAloudOnPhone"
            ),
            WatchCommandType.entries.map { it.name }
        )
        assertEquals(listOf("Speaking", "Paused"), WatchReadAloudPlaybackState.entries.map { it.name })
    }

    @Test
    fun theReadAloudUpdateKeepsItsShape() {
        val update = WatchReadAloudStatusUpdate(status, 1_700_000_000_600L)
        val encoded = """{"status":$statusJson,"updatedAtMillis":1700000000600}"""
        assertEquals(encoded, json.encodeToString(WatchReadAloudStatusUpdate.serializer(), update))
        assertEquals(update, json.decodeFromString(WatchReadAloudStatusUpdate.serializer(), encoded))
    }

    @Test
    fun theAlertRoundTripsInItsShape() {
        val alert = WatchAlert(1_700_000_000_000L, listOf(WatchAlertMatch("123", "may-b", "may/b", "https://may.2chan.net/b/", "スレ", 42)))
        val encoded = """{"detectedAtMillis":1700000000000,"matches":[{"threadId":"123","boardId":"may-b","boardName":"may/b",""" +
            """"boardUrl":"https://may.2chan.net/b/","title":"スレ","replyCount":42}]}"""
        assertEquals(encoded, json.encodeToString(WatchAlert.serializer(), alert))
        assertEquals(alert, json.decodeFromString(WatchAlert.serializer(), encoded))
    }

    @Test
    fun thePhoneStatusFromAnOlderPhoneUsesTheDefaults() {
        assertEquals(WatchPhoneStatus(5L, 0L, true), json.decodeFromString(WatchPhoneStatus.serializer(), """{"phoneNowMillis":5}"""))
        assertEquals(
            """{"phoneNowMillis":5,"watchSentAtMillis":3,"isSupported":false}""",
            json.encodeToString(WatchPhoneStatus.serializer(), WatchPhoneStatus(5L, 3L, false))
        )
    }

    @Test
    fun theShapeLimitsCompiledIntoReleasedWatchesStayTheSame() {
        assertEquals(listOf(80, 20, 50, 5), listOf(
            WATCH_SNAPSHOT_MAX_BOARDS, WATCH_SNAPSHOT_MAX_THREADS,
            WATCH_SNAPSHOT_MAX_WATCH_WORDS, WATCH_SNAPSHOT_MAX_PREVIEW_POSTS_PER_THREAD
        ))
        assertTrue(!snapshot.copy(unreadTotal = 3).hasValidTransportShape())
        assertTrue(!snapshot.copy(watchMatchTotal = 0).hasValidTransportShape())
    }
}
