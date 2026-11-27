package com.valoser.futacha.shared.ui.futaber

import androidx.compose.runtime.Composable
import com.valoser.futacha.shared.util.ImageData

/*
 * L-B8 (M37): the camera menu's OS photo picker. Plan §4.2: 「最近の画像」 is the OS photo picker as 「ライブラリ」 is (no
 * permission to read the photo library: Android's Photo Picker, iOS's PHPicker). Nothing is sent: the picture goes to the
 * attachment as a picked file does (the board's rules, the location data stripped). The menu's 「カメラ」 was taken out
 * (2026-11-04, the user's decision: it was never seen taking a picture in futaber), so the app uses no camera.
 */

/** The message of the photo picker. */
internal const val FUTABER_PHOTO_PICKER_FAILED_MESSAGE = "写真を読み込めませんでした"

