@file:OptIn(coil3.annotation.ExperimentalCoilApi::class)
package com.valoser.futacha.shared.ui.image

import coil3.PlatformContext
import coil3.network.ConnectivityChecker

/** Android: the system's active-network snapshot. iOS: the current Network path. Desktop: always online. */
internal fun createImageConnectivityChecker(platformContext: PlatformContext): ConnectivityChecker =
    ConnectivityChecker(platformContext)
