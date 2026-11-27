package com.valoser.futacha.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valoser.futacha.shared.state.AppStateStore




internal fun appLockSettingDescription(enabled: Boolean): String =
    (if (enabled) "有効です。アプリ起動時にパスワードを要求します。" else "無効です。パスワードを設定すると起動時にロックします。") +
        appLockSnapshotProtectionNotice()?.let { "\n$it" }.orEmpty()
