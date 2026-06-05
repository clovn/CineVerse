package com.cineverse.core.analytics

import platform.Foundation.NSUserDefaults

actual fun getPlatformDarkThemeDefault(): Boolean = true

actual fun savePlatformDarkTheme(enabled: Boolean) {
    NSUserDefaults.standardUserDefaults.setBool(enabled, "is_dark_theme")
}

actual fun loadPlatformDarkTheme(): Boolean? {
    val defaults = NSUserDefaults.standardUserDefaults
    return if (defaults.objectForKey("is_dark_theme") != null) {
        defaults.boolForKey("is_dark_theme")
    } else {
        null
    }
}
