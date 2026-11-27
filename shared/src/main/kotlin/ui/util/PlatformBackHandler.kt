package com.valoser.futacha.shared.ui.util

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Cross-platform hook for reacting to system back gestures/buttons.
 * Android provides a real implementation; other targets can be no-ops.
 */
@Composable
fun PlatformBackHandler(
    enabled: Boolean = true,
    /** iOS-only UIKit edge gesture; Android Back remains enabled when false. */
    iosEdgeGestureEnabled: Boolean = true,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
