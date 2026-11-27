package com.valoser.futacha.wear.sync

/**
 * Asks the phone for a snapshot once when it becomes reachable again (Phase 5, P5-1, candidate 8),
 * so the screen does not wait for the next Data Layer item or a manual sync. The request only reads,
 * and at most one is made per [minIntervalMillis], counting the request made when the app opens.
 * Not thread-safe: call from the main thread.
 */
internal class ReconnectSnapshotRequestPolicy(private val minIntervalMillis: Long = 60_000L) {
    private var lastReachable: Boolean? = null
    private var lastRequestElapsedMillis: Long? = null

    /** Record a request made for another reason (app opened, manual sync). */
    fun recordRequest(nowElapsedMillis: Long) {
        lastRequestElapsedMillis = nowElapsedMillis
    }

    /** Returns true when a snapshot should be requested now; the request is then recorded. */
    fun onReachability(reachable: Boolean, nowElapsedMillis: Long): Boolean {
        val wasUnreachable = lastReachable == false
        lastReachable = reachable
        if (!reachable || !wasUnreachable) return false
        val last = lastRequestElapsedMillis
        if (last != null && nowElapsedMillis - last in 0 until minIntervalMillis) return false
        lastRequestElapsedMillis = nowElapsedMillis
        return true
    }
}
