package com.cineverse.core.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

expect fun getPlatformDarkThemeDefault(): Boolean
expect fun savePlatformDarkTheme(enabled: Boolean)
expect fun loadPlatformDarkTheme(): Boolean?

object ThemeSettings {
    private val _isDarkThemeFlow = MutableStateFlow(loadPlatformDarkTheme() ?: getPlatformDarkThemeDefault())
    val isDarkThemeFlow: StateFlow<Boolean> = _isDarkThemeFlow

    fun isDarkTheme(): Boolean = _isDarkThemeFlow.value

    fun setDarkTheme(enabled: Boolean) {
        savePlatformDarkTheme(enabled)
        _isDarkThemeFlow.value = enabled
    }
}
