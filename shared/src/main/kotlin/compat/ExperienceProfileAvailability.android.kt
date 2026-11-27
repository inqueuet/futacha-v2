package com.valoser.futacha.shared.compat

/** FutachaApplication sets the real value from FLAG_DEBUGGABLE before any profile is read. */
fun platformDefaultPreviewModesEnabled(): Boolean = false

/** FutachaApplication turns it on before any profile is read. */
fun platformDefaultMobileModesEnabled(): Boolean = false
