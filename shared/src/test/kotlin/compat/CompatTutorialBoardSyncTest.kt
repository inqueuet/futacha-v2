package com.valoser.futacha.shared.compat

import com.valoser.futacha.shared.model.BoardSummary
import com.valoser.futacha.shared.ui.board.mockBoardSummaries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The bundled tutorial board survives a round trip ふたちゃ → としあき(仮) → ふたちゃ/ふたばー風 on every platform
 * (2026-10-13 全体検証 3.2). The Android store cannot hold it, so there its absence must not delete it from the
 * AppState; the iOS/desktop stores hold it, and there its absence still means the user deleted it.
 */
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class CompatTutorialBoardSyncTest {
    private val tutorial = mockBoardSummaries.single()
    private val mayUrl = "https://may.2chan.net/b/"
    private val mayCompat = CompatBoard(compatBoardKey(mayUrl), "may", mayUrl, mayUrl, 1)

    @Test
    fun androidLikeStoreKeepsTheTutorialWhenAnotherBoardIsAddedInToshiaki() {
        // The Android store holds official boards only: after the switch it has none, then the board added in としあき(仮).
        val android = modernBoardsToCompatibility(listOf(tutorial))
        assertTrue(android.isEmpty())
        val afterAdd = synchronizeModernBoardsFromCompatibility(listOf(tutorial), listOf(mayCompat.copy(sortOrder = 0)),
            compatibilityHoldsTutorial = false)
        assertEquals(listOf(tutorial.id, mayCompat.key), afterAdd.map(BoardSummary::id))
        assertEquals(tutorial, afterAdd.first())
        // Only the tutorial and an empty store (the state the R8 build showed): the board list is not emptied.
        assertEquals(listOf(tutorial), synchronizeModernBoardsFromCompatibility(listOf(tutorial), emptyList(),
            compatibilityHoldsTutorial = false))
    }

    @Test
    fun androidLikeStoreDoesNotBringBackATutorialTheUserDeleted() {
        // Deleted in ふたちゃ/ふたばー風 (the AppState no longer has it): the synchronization never adds it.
        val synchronized = synchronizeModernBoardsFromCompatibility(
            listOf(BoardSummary("may", "may", "", mayUrl, "")), listOf(mayCompat.copy(sortOrder = 0)),
            compatibilityHoldsTutorial = false
        )
        assertEquals(listOf("may"), synchronized.map(BoardSummary::id))
        assertEquals(emptyList(), synchronizeModernBoardsFromCompatibility(emptyList(), emptyList(),
            compatibilityHoldsTutorial = false))
    }

    /** Replaces the desktop store's round trip: the real Android store says it cannot hold the tutorial. */
    @Test
    fun theAndroidStoreDoesNotHoldTheTutorial() = kotlinx.coroutines.runBlocking {
        val root = java.nio.file.Files.createTempDirectory("tutorial-board-sync").toFile()
        val store = androidCompatibilityStore(com.valoser.futacha.shared.util.createFileSystem(
            androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()))
        try {
            store.initialize()
            kotlin.test.assertFalse(store.holdsBundledTutorialBoard)
        } finally {
            store.close()
            root.deleteRecursively()
        }
    }
}
