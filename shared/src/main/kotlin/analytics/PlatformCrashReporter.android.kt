package com.valoser.futacha.shared.analytics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics

object PlatformCrashReporter {
    private var crashlytics: FirebaseCrashlytics? = null

    fun configure(platformContext: Any?) {
        val context = platformContext as? Context ?: return
        ensureFirebaseInitialized(context) ?: return
        crashlytics = FirebaseCrashlytics.getInstance()
    }

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics?.setCrashlyticsCollectionEnabled(enabled)
    }

    fun setCustomKey(name: String, value: String) {
        // Crash reporting is optional.  Do not synchronously obtain the
        // Firebase singleton from the caller thread while async configuration
        // is still in progress; FirebaseApp.getInstance() can contend with
        // initialization and stall the main thread during cold start.
        crashlytics?.setCustomKey(name, value)
    }

    fun log(message: String) {
        crashlytics?.log(message)
    }

    fun recordException(error: Throwable, sanitizedMessage: String) {
        val sanitized = IllegalStateException(sanitizedMessage).apply {
            stackTrace = error.stackTrace
        }
        crashlytics?.recordException(sanitized)
    }
}
