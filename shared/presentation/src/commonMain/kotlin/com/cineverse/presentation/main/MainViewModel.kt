package com.cineverse.presentation.main

import com.cineverse.domain.repository.UserRepository
import com.cineverse.presentation.base.BaseMviViewModel
import com.cineverse.core.analytics.ThemeSettings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MainState(
    val isOnboardingCompleted: Boolean = false,
    val isAuthorized: Boolean = false,
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean = ThemeSettings.isDarkTheme()
)

sealed class MainIntent {
    data object CompleteOnboarding : MainIntent()
}

class MainViewModel(
    private val userRepository: UserRepository
) : BaseMviViewModel<MainState, MainIntent, Unit>(MainState()) {

    init {
        observeAppState()
    }

    private fun observeAppState() {
        viewModelScope.launch {
            combine(
                userRepository.isOnboardingCompleted(),
                userRepository.getActiveSession(),
                ThemeSettings.isDarkThemeFlow
            ) { onboardingCompleted, session, darkTheme ->
                MainState(
                    isOnboardingCompleted = onboardingCompleted,
                    isAuthorized = session != null,
                    isLoading = false,
                    isDarkTheme = darkTheme
                )
            }.collectLatest { newState ->
                updateState { newState }
            }
        }
    }

    override fun handleIntent(intent: MainIntent) {
        when (intent) {
            MainIntent.CompleteOnboarding -> {
                viewModelScope.launch {
                    userRepository.setOnboardingCompleted(true)
                }
            }
        }
    }
}
