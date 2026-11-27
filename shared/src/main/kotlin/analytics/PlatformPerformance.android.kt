package com.valoser.futacha.shared.analytics

import android.content.Context
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

private var performance: FirebasePerformance? = null

object PlatformPerformance {
    fun configure(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        ensureFirebaseInitialized(context) ?: return
        performance = FirebasePerformance.getInstance()
    }

    fun setPerformanceCollectionEnabled(enabled: Boolean) {
        performance?.isPerformanceCollectionEnabled = enabled
    }

    fun startTrace(name: String): PlatformPerformanceTrace? {
        val trace = performance?.newTrace(name) ?: return null
        trace.start()
        return PlatformPerformanceTrace(trace)
    }
}

class PlatformPerformanceTrace internal constructor(
    private val trace: Trace
) {
    fun putAttribute(name: String, value: String) {
        trace.putAttribute(name, value)
    }

    fun putMetric(name: String, value: Long) {
        trace.putMetric(name, value)
    }

    fun stop() {
        trace.stop()
    }
}
