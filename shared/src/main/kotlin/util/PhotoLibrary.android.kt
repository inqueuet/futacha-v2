package com.valoser.futacha.shared.util
internal fun photoLibrarySaveAvailable(): Boolean = false
internal suspend fun requestPhotoLibraryAddPermission() { error("写真への保存はこの環境では利用できません") }
internal suspend fun saveFileToPhotoLibrary(path: String, video: Boolean) { error("写真への保存はこの環境では利用できません") }
