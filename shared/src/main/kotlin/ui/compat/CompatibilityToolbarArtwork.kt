package com.valoser.futacha.shared.ui.compat

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.valoser.futacha.shared.compat.CompatToolbarSurface
import com.valoser.futacha.shared.compat.COMPAT_THREAD_RESTORE_CLOSED_KEY
import androidx.annotation.DrawableRes
import com.valoser.futacha.shared.R

internal sealed interface CompatToolbarArtwork {
    data class Resource(@param:DrawableRes val drawable: Int) : CompatToolbarArtwork
    data class Vector(val image: ImageVector) : CompatToolbarArtwork
}

/**
 * Resolves the exact surface-specific artwork declared by the four ToolbarData
 * classes in reference app. Keys alone are insufficient: `gallery`, for
 * example, deliberately uses different PNGs on Thread and Viewer.
 */
internal fun compatToolbarArtwork(
    surface: CompatToolbarSurface,
    key: String,
    selected: Boolean = false
): CompatToolbarArtwork = CompatToolbarArtwork.Resource(
    when (surface) {
        CompatToolbarSurface.CATALOG -> when (key) {
            "post" -> R.drawable.menu_ico_catalog_toolbar_post
            "reload" -> R.drawable.menu_ico_catalog_toolbar_reload
            "search" -> R.drawable.menu_ico_catalog_toolbar_search
            "sort" -> R.drawable.menu_ico_catalog_sort
            "board" -> R.drawable.menu_ico_catalog_toolbar_board
            "tab" -> if (selected) {
                R.drawable.menu_ico_catalog_toolbar_tab_update
            } else {
                R.drawable.menu_ico_catalog_toolbar_tab
            }
            "privacy" -> R.drawable.menu_ico_catalog_toolbar_privacy
            "bypass" -> if (selected) {
                R.drawable.menu_ico_catalog_toolbar_server_bypass_on
            } else {
                R.drawable.menu_ico_catalog_toolbar_server_bypass_off
            }
            "check" -> R.drawable.menu_ico_catalog_toolbar_check
            "undo" -> R.drawable.catalog_undo
            "dropped" -> R.drawable.menu_ico_catalog_toolbar_dropped
            "quickng" -> if (selected) R.drawable.ngon else R.drawable.ngoff
            "drawer" -> R.drawable.drawer
            "other" -> R.drawable.menu_ico_post_toolbar_other
            else -> return CompatToolbarArtwork.Vector(compatToolbarIcon(key))
        }

        CompatToolbarSurface.THREAD -> when (key) {
            "post" -> R.drawable.menu_ico_thread_toolbar_post
            "reload" -> R.drawable.menu_ico_thread_toolbar_reload
            "undo" -> R.drawable.catalog_undo
            "search" -> R.drawable.menu_ico_thread_toolbar_search
            "top" -> R.drawable.menu_ico_thread_toolbar_vertical_align_top
            "page_up" -> R.drawable.menu_ico_thread_toolbar_arrow_upward
            "page_down" -> R.drawable.menu_ico_thread_toolbar_arrow_downward
            "bottom" -> R.drawable.menu_ico_thread_toolbar_vertical_align_bottom
            "gallery" -> R.drawable.menu_ico_thread_toolbar_gallery
            "tab" -> if (selected) {
                R.drawable.menu_ico_thread_toolbar_tab_update
            } else {
                R.drawable.menu_ico_thread_toolbar_tab
            }
            "privacy" -> R.drawable.menu_ico_thread_toolbar_privacy
            "extract" -> R.drawable.menu_ico_thread_toolbar_extract
            "bypass" -> if (selected) {
                R.drawable.menu_ico_thread_toolbar_server_bypass_on
            } else {
                R.drawable.menu_ico_thread_toolbar_server_bypass_off
            }
            "scroll" -> R.drawable.menu_ico_thread_toolbar_scroll
            "check" -> R.drawable.menu_ico_thread_toolbar_check
            "close" -> R.drawable.menu_ico_thread_toolbar_close
            "quickng" -> if (selected) R.drawable.ngon else R.drawable.ngoff
            "drawer" -> R.drawable.drawer
            "autoscroll" -> if (selected) R.drawable.pause else R.drawable.autoscroll
            // reference app 18-20 draws its restore-closed button with catalog_undo.
            COMPAT_THREAD_RESTORE_CLOSED_KEY -> R.drawable.catalog_undo
            "other" -> R.drawable.menu_ico_post_toolbar_other
            else -> return CompatToolbarArtwork.Vector(compatToolbarIcon(key))
        }

        CompatToolbarSurface.VIEWER -> when (key) {
            "download" -> R.drawable.menu_ico_viewer_toolbar_dl
            "search" -> R.drawable.menu_ico_viewer_toolbar_search
            "back" -> R.drawable.menu_ico_viewer_toolbar_back
            "gallery" -> R.drawable.menu_ico_viewer_toolbar_gallery
            "left" -> R.drawable.menu_ico_viewer_toolbar_left
            "right" -> R.drawable.menu_ico_viewer_toolbar_right
            "share" -> R.drawable.menu_ico_viewer_toolbar_share
            "info" -> R.drawable.menu_ico_viewer_toolbar_info
            "screen" -> R.drawable.menu_ico_viewer_toolbar_screen
            "privacy" -> R.drawable.menu_ico_catalog_toolbar_privacy
            "other" -> R.drawable.menu_ico_post_toolbar_other
            else -> return CompatToolbarArtwork.Vector(compatToolbarIcon(key))
        }

        CompatToolbarSurface.POST -> when (key) {
            "send" -> R.drawable.menu_ico_post_send
            "attach" -> R.drawable.menu_ico_post_attach
            "attach_clear" -> R.drawable.menu_ico_post_attach_clear
            "pallete" -> R.drawable.menu_ico_post_pallete
            "sio" -> R.drawable.menu_ico_post_ups
            "voice_input" -> R.drawable.menu_ico_post_voice_input
            "network_info" -> R.drawable.menu_ico_post_network_info
            "model_info" -> R.drawable.menu_ico_post_model_info
            "reset" -> R.drawable.menu_ico_post_reset
            "discard" -> R.drawable.menu_ico_post_discard
            "other" -> R.drawable.menu_ico_post_toolbar_other
            else -> return CompatToolbarArtwork.Vector(compatToolbarIcon(key))
        }
    }
)

@Composable
internal fun CompatToolbarArtworkIcon(
    artwork: CompatToolbarArtwork,
    contentDescription: String?,
    tint: Color,
    preserveResourceColors: Boolean = true,
    modifier: Modifier = Modifier
) {
    when (artwork) {
        is CompatToolbarArtwork.Resource -> Icon(
            painter = compatResourcePainter(artwork.drawable),
            contentDescription = contentDescription,
            // The reference PNGs are not all monochrome: NG-on and the tab
            // update badge contain intentional red pixels.  Applying a
            // Compose tint here would silently turn them white/yellow.
            tint = if (preserveResourceColors) Color.Unspecified else tint,
            // Preserve the source RGB values while still honoring the
            // reference toolbar's disabled/inactive alpha.
            modifier = if (preserveResourceColors) {
                modifier.graphicsLayer { alpha = tint.alpha }
            } else {
                modifier
            }
        )

        is CompatToolbarArtwork.Vector -> Icon(
            imageVector = artwork.image,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier
        )
    }
}
