package com.valoser.futacha.shared.util

import android.os.Build

/**
 * Platform helper to gate behavior in common UI logic.
 */
fun isAndroid(): Boolean = true

/** Android 8/10's legacy IME consumes an extra physical BACK before Compose sees it. */
fun isLegacyCompatImeBackBehavior(): Boolean =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q

/** True only in the installed desktop application (not JVM logic tests). */
fun isDesktop(): Boolean = false
