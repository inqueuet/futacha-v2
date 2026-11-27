package com.valoser.futacha.shared.ui.futaber

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * On iPhone the strip at the bottom of the screen (the home-indicator area) belongs to the host
 * view, not to Compose, and shows the system background. This gives that area the mode's bar
 * colour while the ふたばー screens are shown, and releases it when they leave, so the other
 * modes keep the host as it was. It does nothing on the other platforms.
 */
@Composable
internal fun ApplyFutaberHostBackground(color: Color) = Unit

/**
 * Android: the navigation bar at the bottom takes the mode's bar colour and its glyphs follow that colour, which can
 * differ from the status bar's (ふたば: a red top, a white bottom bar). Called after [com.valoser.futacha.shared.ui.theme.ApplyFutachaSystemBars],
 * whose navigation-bar settings it overrides; the shared function and the other modes are left as they are. It does
 * nothing on the other platforms (iPhone's bottom strip is [ApplyFutaberHostBackground]).
 */
@Composable
@Suppress("DEPRECATION")
internal fun ApplyFutaberNavigationBar(color: Color, darkIcons: Boolean, statusColor: Color) {
    val view = LocalView.current
    SideEffect {
        val window = view.context.findFutaberActivity()?.window ?: return@SideEffect
        window.navigationBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = darkIcons
    }
}

private tailrec fun Context.findFutaberActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findFutaberActivity()
    else -> null
}
