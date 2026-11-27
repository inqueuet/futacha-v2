package com.valoser.futacha.shared.media

/** OutOfMemoryError where the platform can recover from it (JVM/Android). */
internal fun Throwable.isMemoryExhaustion(): Boolean = this is OutOfMemoryError

// UnsatisfiedLinkError on the first load, NoClassDefFoundError/ExceptionInInitializerError afterwards.
/** A missing or unloadable native library: UnsatisfiedLinkError and the other LinkageErrors. */
internal fun Throwable.isNativeLinkFailure(): Boolean = this is LinkageError
