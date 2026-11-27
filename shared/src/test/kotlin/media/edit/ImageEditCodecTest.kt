package com.valoser.futacha.shared.media.edit

import com.valoser.futacha.shared.compat.androidCompatibilityStore
import com.valoser.futacha.shared.compat.close

import coil3.PlatformContext
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class ImageEditCodecTest {
    @Test fun nativePreviewAndJpegPixelsMatch() = runBlocking { ImageEditCodecContract.nativePixelsPreviewAndJpegExport(org.robolectric.RuntimeEnvironment.getApplication()) }
    @Test fun allEightExifOrientationsMatch() = runBlocking { ImageEditCodecContract.exifRotationAndMirroringAreAppliedBeforeEditing(org.robolectric.RuntimeEnvironment.getApplication()) }
    @Test fun independentSettingsRejectStaleSessions() = runBlocking { ImageEditCodecContract.featureGatesAreIndependentAndOldSessionsCannotExport(org.robolectric.RuntimeEnvironment.getApplication()) }
    @Test fun localStaticWebpCanBeEdited() = runBlocking { ImageEditCodecContract.staticWebpFromDeviceIsSupported(org.robolectric.RuntimeEnvironment.getApplication()) }
    @Test fun promptOffRetainsMetadataAndEditedPixels() = runBlocking { ImageEditCodecContract.promptOffPreservesTagsAndEditedPixelsWithoutCache(org.robolectric.RuntimeEnvironment.getApplication()) }
}
