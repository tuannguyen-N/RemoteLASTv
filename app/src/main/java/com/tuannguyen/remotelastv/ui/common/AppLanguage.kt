package com.tuannguyen.remotelastv.ui.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String,
) {
    SYSTEM("", "System default", ""),
    ENGLISH("en", "English", "English"),
    VIETNAMESE("vi", "Tiếng Việt", "Vietnamese"),
    CHINESE("zh-CN", "简体中文", "Chinese (Simplified)"),
    KOREAN("ko", "한국어", "Korean"),
    ;

    fun apply() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    companion object {
        fun current(): AppLanguage {
            val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return entries.firstOrNull { it != SYSTEM && tags.startsWith(it.tag) } ?: SYSTEM
        }
    }
}
