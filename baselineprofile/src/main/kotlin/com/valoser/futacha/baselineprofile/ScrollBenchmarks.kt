package com.valoser.futacha.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Posts in the benchmark's large thread (the tutorial thread repeated, benchmark builds only). */
internal const val LARGE_THREAD_POSTS = 1_200

@RunWith(AndroidJUnit4::class)
class FutachaLargeThreadScrollBenchmark : LargeThreadScrollBenchmark(FutachaProfile.FUTACHA)

@RunWith(AndroidJUnit4::class)
class ToshiakiCompatLargeThreadScrollBenchmark : LargeThreadScrollBenchmark(FutachaProfile.TOSHIAKI_COMPAT)

@RunWith(AndroidJUnit4::class)
class FutaberLargeThreadScrollBenchmark : LargeThreadScrollBenchmark(FutachaProfile.FUTABER)

@RunWith(AndroidJUnit4::class)
class FtutterLargeThreadScrollBenchmark : LargeThreadScrollBenchmark(FutachaProfile.FTUTTER)

abstract class LargeThreadScrollBenchmark(private val profile: FutachaProfile) {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun selectProfile() {
        ensureProfile(profile, largeThreadPosts = LARGE_THREAD_POSTS)
    }

    @Test
    fun compilationBaselineProfile() {
        rule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
            iterations = BENCHMARK_ITERATIONS,
            startupMode = StartupMode.WARM,
            setupBlock = {
                startActivityAndWait()
                openTutorialThread(profile)
            }
        ) {
            scrollLargeThread()
        }
    }
}
