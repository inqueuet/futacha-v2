package com.valoser.futacha.shared.ui.util

import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.ui.Modifier

/** Reserve this layout region from platform-owned system gestures when supported. */
internal fun Modifier.platformSystemGestureExclusion(): Modifier = systemGestureExclusion()
