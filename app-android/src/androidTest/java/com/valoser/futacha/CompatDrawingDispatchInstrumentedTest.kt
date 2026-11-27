@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.valoser.futacha

import android.graphics.BitmapFactory
import android.os.SystemClock
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.valoser.futacha.shared.ui.compat.CompatDrawingPoint
import com.valoser.futacha.shared.ui.compat.CompatDrawingStroke
import com.valoser.futacha.shared.ui.compat.CompatDrawingStrokeCache
import com.valoser.futacha.shared.ui.compat.detectCompatDrawingStrokes
import com.valoser.futacha.shared.ui.compat.drawCompatDrawingStroke
import com.valoser.futacha.shared.ui.compat.renderCompatDrawingPng
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The Android replacement of the desktop CompatDrawingDispatchTest (skikoTest, removed with the desktop target):
 * the same strokes go through Android's real MotionEvent → Compose dispatch, on a 344×135-pixel canvas.
 */
class CompatDrawingDispatchInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val paper = 0xFFF0E0D6.toInt()
    private val ink = 0xFF800000.toInt()
    private val active = mutableStateListOf<Offset>()
    private val completed = mutableStateListOf<List<Offset>>()
    private val cache = CompatDrawingStrokeCache()
    private var downTime = 0L
    private var time = 0L
    private var origin = Offset.Zero

    private fun stroke(points: List<Offset>) = CompatDrawingStroke(ink, 4f, points.map { CompatDrawingPoint(it.x, it.y) })

    private fun drawing(block: () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Canvas(Modifier.size(with(density) { 344.toDp() }, with(density) { 135.toDp() })
                    .testTag("dispatch-canvas")
                    .pointerInput(Unit) {
                        detectCompatDrawingStrokes(
                            onStart = { active.clear(); active.add(it) },
                            onPoint = { if (active.lastOrNull() != it) active.add(it) },
                            onEnd = { completed.add(active.toList()); active.clear() },
                            onCancel = { active.clear() }
                        )
                    }) {
                    drawRect(Color(paper))
                    cache.draw(this, completed.map { stroke(it) })
                    if (active.isNotEmpty()) drawCompatDrawingStroke(stroke(active))
                }
            }
        }
        val bounds = rule.onNodeWithTag("dispatch-canvas").fetchSemanticsNode().boundsInWindow
        assertEquals(344f, bounds.width, 0.5f)
        origin = Offset(bounds.left, bounds.top)
        downTime = SystemClock.uptimeMillis()
        time = downTime
        block()
    }

    private fun event(action: Int, point: Offset, history: List<Offset> = emptyList()) {
        time += 16L
        rule.runOnUiThread {
            val first = history.firstOrNull() ?: point
            val event = MotionEvent.obtain(downTime, if (history.isEmpty()) time else time - 8,
                action, origin.x + first.x, origin.y + first.y, 0)
            if (history.isNotEmpty()) {
                // Different real samples may share a rounded millisecond timestamp.
                (history.drop(1) + point).forEachIndexed { i, p ->
                    event.addBatch(time - 8 + (i + 1) * 8 / (history.size), origin.x + p.x, origin.y + p.y, 1f, 1f, 0)
                }
            }
            try { rule.activity.dispatchTouchEvent(event) } finally { event.recycle() }
        }
        rule.waitForIdle()
    }

    private fun preview(): IntArray {
        val map = rule.onNodeWithTag("dispatch-canvas").captureToImage().toPixelMap()
        assertEquals(344, map.width); assertEquals(135, map.height)
        return IntArray(344 * 135) { map[it % 344, it / 344].toArgb() }
    }

    private fun png(): IntArray = runBlocking {
        val data = renderCompatDrawingPng(completed.map { stroke(it) }, paper, 344, 135).getOrThrow()
        val bitmap = BitmapFactory.decodeByteArray(data.bytes, 0, data.bytes.size)
        assertEquals(344, bitmap.width); assertEquals(135, bitmap.height)
        IntArray(344 * 135).also { bitmap.getPixels(it, 0, 344, 0, 0, 344, 135) }
    }

    @Test fun subpixelExcursionReturningToTheSamePositionReachesTheHandlerInOrder() = drawing {
        val start = Offset(20f, 40f)
        val anchor = Offset(24f, 40f)
        val history = listOf(Offset(24.125f, 40.25f), Offset(24.25f, 39.75f), Offset(24.125f, 40.25f))
        event(MotionEvent.ACTION_DOWN, start)
        event(MotionEvent.ACTION_MOVE, anchor)
        event(MotionEvent.ACTION_MOVE, anchor, history)
        assertEquals(listOf(start, anchor) + history + anchor, active.toList())
        event(MotionEvent.ACTION_UP, Offset(25f, 41f))
        assertEquals(listOf(start, anchor) + history + listOf(anchor, Offset(25f, 41f)), completed.single())
    }

    @Test fun releaseOnlyMovementAndAllItsHistoryReachTheHandler() = drawing {
        val start = Offset(20f, 40f)
        val end = Offset(100f, 80f)
        val history = listOf(Offset(40f, 70f), Offset(60f, 20f), Offset(80f, 60f))
        event(MotionEvent.ACTION_DOWN, start)
        event(MotionEvent.ACTION_UP, end, history)
        assertEquals(listOf(start) + history + end, completed.single())
        assertTrue(active.isEmpty())
    }

    @Test fun closedSubframeLoopAndReleaseTailRemainVisibleAndInTheActualPng() = drawing {
        val start = Offset(20f, 40f)
        val anchor = Offset(30f, 40f)
        val loop = listOf(Offset(60f, 90f), Offset(100f, 20f), Offset(140f, 85f))
        val tail = listOf(Offset(180f, 30f), Offset(220f, 90f))
        val end = Offset(300f, 110f)
        event(MotionEvent.ACTION_DOWN, start)
        event(MotionEvent.ACTION_MOVE, anchor)
        event(MotionEvent.ACTION_MOVE, anchor, loop)
        // The intermediate loop must already be visible before the finger lifts.
        val beforeUp = preview()
        loop.forEach { assertEquals("active bend $it", ink, beforeUp[it.y.toInt() * 344 + it.x.toInt()]) }
        event(MotionEvent.ACTION_UP, end, tail)
        assertEquals(listOf(start, anchor) + loop + listOf(anchor) + tail + end, completed.single())
        for (pixels in listOf(preview(), png())) {
            (loop + tail + end).forEach { p ->
                assertEquals("missing bend or endpoint $p", ink, pixels[p.y.toInt() * 344 + p.x.toInt()])
            }
            assertEquals("Do not invent a straight or predicted continuation", paper, pixels[15 * 344 + 300])
        }
    }

    @Test fun hundredsOfSamplesInOneFrameAreNotReducedToTheLastPosition() = drawing {
        val anchor = Offset(40f, 50f)
        val samples = (1..512).map { Offset(40f + it % 13 * 0.125f, 50f + it % 11 * 0.125f) }
        event(MotionEvent.ACTION_DOWN, Offset(20f, 50f))
        event(MotionEvent.ACTION_MOVE, anchor)
        event(MotionEvent.ACTION_MOVE, anchor, samples)
        event(MotionEvent.ACTION_UP, Offset(100f, 70f))
        assertEquals(listOf(Offset(20f, 50f), anchor) + samples + listOf(anchor, Offset(100f, 70f)), completed.single())
    }

    @Test fun eightyFastStrokesDoNotLoseTheirSeparateEndpoints() = drawing {
        repeat(80) { i ->
            val start = Offset(20f + i, 40f)
            downTime = time + 16
            event(MotionEvent.ACTION_DOWN, start)
            event(MotionEvent.ACTION_UP, start + Offset(0.125f, 0.25f))
        }
        assertEquals(80, completed.size)
        completed.forEachIndexed { i, points ->
            assertEquals(listOf(Offset(20f + i, 40f), Offset(20.125f + i, 40.25f)), points)
        }
    }
}
