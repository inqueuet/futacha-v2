package com.valoser.futacha.shared.ui.board
internal fun logVideoPlaybackDiagnostic(event: String, detail: String) = Unit

/** One capability check shared by source acquisition and the platform resource factories. */
internal fun platformVideoPlaybackCapabilityError(
    videoUrl: String,
    resolvedInfo: com.valoser.futacha.shared.media.source.OriginalMediaInfo? = null
): VideoPlaybackError? = null
