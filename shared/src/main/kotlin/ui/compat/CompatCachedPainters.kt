package com.valoser.futacha.shared.ui.compat

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import java.util.concurrent.ConcurrentHashMap

/**
 * Painters for the small bundled pictures that every row draws (the reply mark,
 * the board icon, "no thumbnail"). Compose Multiplatform kept one decoded copy
 * for the whole app; Android's painterResource reads the resource again for each
 * row. These keep one copy per picture and night mode, so the rows look the same
 * and only the work is shared.
 */
private object CompatPainterCache {
    val vectors = ConcurrentHashMap<Long, ImageVector>()
    val bitmaps = ConcurrentHashMap<Long, ImageBitmap>()
    fun key(id: Int, night: Int): Long = (id.toLong() shl 8) or night.toLong()
}

@Composable
internal fun compatVectorPainter(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val night = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val vector = remember(id, night) {
        CompatPainterCache.vectors.getOrPut(CompatPainterCache.key(id, night)) {
            ImageVector.vectorResource(context.theme, context.resources, id)
        }
    }
    return rememberVectorPainter(vector)
}

@Composable
internal fun compatBitmapPainter(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val night = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return remember(id, night) {
        BitmapPainter(
            CompatPainterCache.bitmaps.getOrPut(CompatPainterCache.key(id, night)) {
                ImageBitmap.imageResource(context.resources, id)
            }
        )
    }
}

/** [compatVectorPainter] or [compatBitmapPainter], chosen by the resource file (".xml" is a vector). */
@Composable
internal fun compatResourcePainter(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val isVector = remember(id) {
        val value = android.util.TypedValue()
        context.resources.getValue(id, value, true)
        value.string?.endsWith(".xml") == true
    }
    return if (isVector) compatVectorPainter(id) else compatBitmapPainter(id)
}
