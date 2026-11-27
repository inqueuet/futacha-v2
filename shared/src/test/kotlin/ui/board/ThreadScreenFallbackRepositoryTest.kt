package com.valoser.futacha.shared.ui.board

import com.valoser.futacha.shared.model.BoardSummary
import com.valoser.futacha.shared.repo.mock.SharedFakeBoardRepository
import kotlin.test.Test
import kotlin.test.assertSame

class ThreadScreenFallbackRepositoryTest {
    private val tutorial = BoardSummary("t", "チュートリアル", "", "https://www.example.com/t/futaba.php", "")

    private fun bundle() = buildThreadScreenEnvironmentBundle(
        repository = null,
        autoSavedThreadRepository = null,
        fileSystem = null,
        manualSaveDirectory = "Documents",
        manualSaveLocation = null,
        history = emptyList(),
        threadId = "1",
        board = tutorial,
        resolvedThreadUrlOverride = null
    )

    // The first-load effect is keyed on the repository: a new fallback per rebuild reloaded the
    // tutorial thread on every frame, and each load rewrote the history (Phase 4 scroll test).
    @Test
    fun theTutorialFallbackIsTheSameRepositoryOnEveryRebuild() {
        assertSame(bundle().activeRepository, bundle().activeRepository)
        assertSame(SharedFakeBoardRepository, bundle().activeRepository)
    }
}
