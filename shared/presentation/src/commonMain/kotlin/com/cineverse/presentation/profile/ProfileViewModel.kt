package com.cineverse.presentation.profile

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.ProfileStats
import com.cineverse.domain.model.UserProfile
import com.cineverse.domain.repository.UserRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileState(
    val user: UserProfile? = null,
    val isAuthorized: Boolean = false,
    val stats: ProfileStats? = null,
    val usernameInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProfileIntent {
    data object Login : ProfileIntent()
    data object Register : ProfileIntent()
    data object Logout : ProfileIntent()
    data class TypeUsername(val username: String) : ProfileIntent()
    data class TypePassword(val password: String) : ProfileIntent()
}

sealed class ProfileEffect {
    data object AuthSuccess : ProfileEffect()
    data class AuthError(val message: String) : ProfileEffect()
}

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {

    init {
        observeSession()
    }

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.TypeUsername -> {
                updateState { it.copy(usernameInput = intent.username) }
            }
            is ProfileIntent.TypePassword -> {
                updateState { it.copy(passwordInput = intent.password) }
            }
            is ProfileIntent.Login -> executeLogin()
            is ProfileIntent.Register -> executeRegister()
            is ProfileIntent.Logout -> executeLogout()
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            userRepository.getActiveSession().collectLatest { activeUser ->
                if (activeUser != null) {
                    analyticsTracker.trackEvent("launch_profile")
                    updateState { it.copy(user = activeUser, isAuthorized = true, usernameInput = "", passwordInput = "") }
                    loadStats(activeUser.username)
                } else {
                    analyticsTracker.trackEvent("launch_auth")
                    updateState { it.copy(user = null, isAuthorized = false, stats = null) }
                }
            }
        }
    }

    private fun loadStats(username: String) {
        viewModelScope.launch {
            try {
                val userStats = userRepository.getProfileStats(username)
                updateState { it.copy(stats = userStats) }
            } catch (e: Exception) {
                
            }
        }
    }

    private fun executeLogin() {
        val username = state.value.usernameInput
        val password = state.value.passwordInput
        if (username.isBlank() || password.isBlank()) {
            sendEffect(ProfileEffect.AuthError("Username and password cannot be empty"))
            return
        }
        
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            try {
                userRepository.login(username, password)
                updateState { it.copy(isLoading = false) }
                sendEffect(ProfileEffect.AuthSuccess)
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                sendEffect(ProfileEffect.AuthError(e.message ?: "Authentication failed"))
            }
        }
    }

    private fun executeRegister() {
        val username = state.value.usernameInput
        val password = state.value.passwordInput
        if (username.isBlank() || password.isBlank()) {
            sendEffect(ProfileEffect.AuthError("Username and password cannot be empty"))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            try {
                userRepository.register(username, password)
                updateState { it.copy(isLoading = false) }
                sendEffect(ProfileEffect.AuthSuccess)
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                sendEffect(ProfileEffect.AuthError(e.message ?: "Registration failed"))
            }
        }
    }

    private fun executeLogout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}
