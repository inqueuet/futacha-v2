package com.valoser.futacha.shared.compat

/** Nijilog compares NFKC-normalized, case-folded catalog titles (not whole thread bodies). */
internal fun normalizeCompatWatchText(raw: String): String =
    java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFKC).trim().uppercase(java.util.Locale.ROOT)
