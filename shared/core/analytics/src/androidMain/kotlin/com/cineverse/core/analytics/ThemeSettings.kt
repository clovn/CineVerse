package com.cineverse.core.analytics

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AndroidSettingsHelper : KoinComponent {
    val context: Context by inject()
}

actual fun getPlatformDarkThemeDefault(): Boolean = true

actual fun savePlatformDarkTheme(enabled: Boolean) {
    val context = AndroidSettingsHelper.context
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("is_dark_theme", enabled).apply()
}

actual fun loadPlatformDarkTheme(): Boolean? {
    val context = AndroidSettingsHelper.context
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    return if (prefs.contains("is_dark_theme")) {
        prefs.getBoolean("is_dark_theme", true)
    } else {
        null
    }
}
