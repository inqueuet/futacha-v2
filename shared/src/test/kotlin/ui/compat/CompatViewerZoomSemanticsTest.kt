package com.valoser.futacha.shared.ui.compat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class CompatViewerZoomSemanticsTest {
    @get:Rule val compose = createComposeRule()

    private fun SemanticsNode.descriptions(): List<String> =
        listOfNotNull(config.getOrNull(SemanticsProperties.StateDescription)) +
            children.flatMap { it.descriptions() }

    @Test
    fun zoomDescriptionFollowsTransformWithoutRecomposition() {
        val transform = mutableStateOf(CompatViewerTransform())
        var compositions = 0
        compose.setContent {
            SideEffect { compositions++ }
            Box(Modifier.size(50.dp).compatViewerZoomSemantics(true) { transform.value })
        }
        fun descriptions(): List<String> {
            Snapshot.sendApplyNotifications()
            compose.waitForIdle()
            return compose.onRoot(useUnmergedTree = true).fetchSemanticsNode().descriptions()
        }
        assertEquals(listOf("拡大率 100% 位置 0,0"), descriptions())
        val initialCompositions = compositions

        transform.value = CompatViewerTransform(2.5f, Offset(10f, -20f))
        assertEquals(listOf("拡大率 250% 位置 10,-20"), descriptions())
        transform.value = CompatViewerTransform(6f, Offset.Zero)
        assertEquals(listOf("拡大率 600% 位置 0,0"), descriptions())

        // The pinch state is observed by the semantics node, not the composition.
        assertEquals(initialCompositions, compositions)
    }
}
