package com.valoser.futacha.wear.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class ReconnectSnapshotRequestPolicyTest {
    @Test
    fun requestsOnlyWhenComingBackAfterBeingUnreachable() {
        val policy = ReconnectSnapshotRequestPolicy()
        assertEquals(false, policy.onReachability(true, 0L))       // first check: app open already asked
        assertEquals(false, policy.onReachability(true, 30_000L))  // still connected
        assertEquals(false, policy.onReachability(false, 60_000L))
        assertEquals(true, policy.onReachability(true, 90_000L))   // reconnected
        assertEquals(false, policy.onReachability(true, 120_000L))
    }

    @Test
    fun atMostOnePerMinuteIncludingTheOpeningRequest() {
        val policy = ReconnectSnapshotRequestPolicy()
        policy.recordRequest(0L)
        policy.onReachability(false, 10_000L)
        assertEquals(false, policy.onReachability(true, 40_000L))  // 40 s after the opening request
        policy.onReachability(false, 50_000L)
        assertEquals(true, policy.onReachability(true, 70_000L))
        policy.onReachability(false, 80_000L)
        assertEquals(false, policy.onReachability(true, 100_000L)) // 30 s after the last
    }

    @Test
    fun aFailedCheckCountsAsUnreachable() {
        val policy = ReconnectSnapshotRequestPolicy()
        policy.onReachability(false, 0L)
        assertEquals(true, policy.onReachability(true, 30_000L))
    }
}
