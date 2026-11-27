package com.valoser.futacha.shared.repo.mock

/**
 * Benchmark builds only. The app's BenchmarkFixtureReceiver (disabled in shipping builds) sets this so
 * the tutorial thread can be measured as a large thread. 0 keeps the normal checked-in thread.
 */
object BenchmarkFixtureHooks {
    const val MAX_THREAD_POSTS = 2_000

    @Volatile
    var largeThreadPostCount: Int = 0
        set(value) {
            field = value.coerceIn(0, MAX_THREAD_POSTS)
        }
}
