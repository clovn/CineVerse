package com.cineverse.presentation.home

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.Movie
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.launch

enum class HomeTab {
    Trending, NowPlaying
}

data class HomeState(
    val movies: List<Movie> = emptyList(),
    val nowPlaying: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: HomeTab = HomeTab.Trending
)

sealed class HomeIntent {
    data object LoadData : HomeIntent()
    data class ChangeTab(val tab: HomeTab) : HomeIntent()
}

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
}

class HomeViewModel(
    private val movieRepository: MovieRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        analyticsTracker.trackEvent("launch_home")
        sendIntent(HomeIntent.LoadData)
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadData -> loadContent()
            is HomeIntent.ChangeTab -> {
                updateState { it.copy(currentTab = intent.tab) }
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            try {
                val trending = movieRepository.getTrending()
                val nowPlaying = movieRepository.getNowPlaying()
                updateState {
                    it.copy(
                        movies = trending,
                        nowPlaying = nowPlaying,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                sendEffect(HomeEffect.ShowError(e.message ?: "Failed to load data"))
            }
        }
    }
}
