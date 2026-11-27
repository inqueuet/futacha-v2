package com.valoser.futacha.shared.ui.ftutter

import androidx.compose.ui.window.DialogProperties

/** Android supplies its IME insets to the content; other hosts keep their default dialog behavior. */
internal fun ftutterInputDialogProperties() = DialogProperties(decorFitsSystemWindows = false)

/** The write sheet: full width, laid out under the keyboard by the sheet itself (T-B3). */
internal fun ftutterSheetDialogProperties() = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnClickOutside = false)
