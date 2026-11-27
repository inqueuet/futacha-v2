package com.valoser.futacha.shared.ui.media

import androidx.compose.ui.window.DialogProperties

/** Fullscreen editors handle system bars with safeDrawingPadding inside the dialog. */
internal fun mediaEditorDialogProperties() = DialogProperties(
    usePlatformDefaultWidth = false,
    // Let safeDrawingPadding receive the system-bar insets in fullscreen dialogs.
    decorFitsSystemWindows = false
)
