package com.valoser.futacha.shared.ui.ftutter

/**
 * 公開用のソースには、ふたったーのバックアップ（.ftt）を読むための鍵を入れていない。
 * available が false なので、設定に取り込みの行は出ない。
 */
internal object FtutterBackupCipher {
    const val available: Boolean = false

    val key: ByteArray get() = ByteArray(32)
    val iv: ByteArray get() = ByteArray(16)
}
