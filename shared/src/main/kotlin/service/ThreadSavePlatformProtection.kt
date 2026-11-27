package com.valoser.futacha.shared.service

import com.valoser.futacha.shared.model.SaveProgress
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

private val userInitiatedThreadSaveMutex = Mutex()

class ThreadSaveAlreadyRunningException : IllegalStateException("別の保存を実行中です")

/**
 * Runs one user initiated thread save at a time across every screen and mode.
 *
 * Both platform protectors own a single notification/background-task surface.
 * Rejecting a second save avoids replacing the first save's cancellation target
 * after the user navigates to another thread while it is still running.
 */
suspend fun <T> runProtectedThreadSave(
    title: String,
    progress: StateFlow<SaveProgress?>,
    block: suspend () -> T
): T = runExclusiveUserThreadSave {
    withThreadSavePlatformProtection(title, progress, block)
}

internal suspend fun <T> runExclusiveUserThreadSave(block: suspend () -> T): T {
    if (!userInitiatedThreadSaveMutex.tryLock()) throw ThreadSaveAlreadyRunningException()
    return try {
        block()
    } finally {
        userInitiatedThreadSaveMutex.unlock()
    }
}

