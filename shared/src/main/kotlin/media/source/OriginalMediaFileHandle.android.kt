package com.valoser.futacha.shared.media.source

import okio.FileHandle
import okio.FileSystem
import okio.Path

/** A streaming reader must remain usable when its completed cache entry is renamed. */
internal fun FileSystem.openOriginalMediaReadHandle(path: Path): FileHandle = openReadOnly(path)
