package com.valoser.futacha.shared.ai

import kotlin.test.Test
import kotlin.test.assertEquals

// Moved from shared FutachaAiCommandTest (Phase 3): decodeAiQueryValue is internal to :core-wearable.
class FutachaAiQueryDecodeTest {
    @Test
    fun percentDecoderPreservesUnicodeAndMalformedEscapesWithoutBoxedByteList() {
        assertEquals("テスト value", decodeAiQueryValue("%E3%83%86%E3%82%B9%E3%83%88+value"))
        assertEquals("bad%2Gvalue", decodeAiQueryValue("bad%2Gvalue"))
    }
}
