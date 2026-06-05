package com.cineverse.presentation.watchlist

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.Movie
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class WatchlistTab {
    Favorites, WatchLater
}

data class WatchlistState(
    val favorites: List<Movie> = emptyList(),
    val watchLater: List<Movie> = emptyList(),
    val selectedTab: WatchlistTab = WatchlistTab.Favorites,
    val isLoading: Boolean = false
)

sealed class WatchlistIntent {
    data object LoadLists : WatchlistIntent()
    data class ChangeTab(val tab: WatchlistTab) : WatchlistIntent()
    data class RemoveMovie(val movieId: Int, val tab: WatchlistTab) : WatchlistIntent()
}

sealed class WatchlistEffect {
    data class ShowMessage(val message: String) : WatchlistEffect()
}

class WatchlistViewModel(
    private val movieRepository: MovieRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<WatchlistState, WatchlistIntent, WatchlistEffect>(WatchlistState()) {

    init {
        analyticsTracker.trackEvent("launch_watchlist")
        sendIntent(WatchlistIntent.LoadLists)
    }

    override fun handleIntent(intent: WatchlistIntent) {
        when (intent) {
            is WatchlistIntent.LoadLists -> observeWatchlists()
            is WatchlistIntent.ChangeTab -> {
                updateState { it.copy(selectedTab = intent.tab) }
            }
            is WatchlistIntent.RemoveMovie -> executeRemove(intent.movieId, intent.tab)
        }
    }

    private fun observeWatchlists() {
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            movieRepository.getFavoriteMovies().collectLatest { list ->
                updateState { it.copy(favorites = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            movieRepository.getWatchLaterMovies().collectLatest { list ->
                updateState { it.copy(watchLater = list, isLoading = false) }
            }
        }
    }

    private fun executeRemove(movieId: Int, tab: WatchlistTab) {
        viewModelScope.launch {
            try {

                val movie = state.value.favorites.firstOrNull { it.id == movieId }
                    ?: state.value.watchLater.firstOrNull { it.id == movieId }
                    ?: return@launch
                
                if (tab == WatchlistTab.Favorites) {
                    movieRepository.toggleFavorite(movie)
                    sendEffect(WatchlistEffect.ShowMessage("Removed '${movie.title}' from Favorites"))
                } else {
                    movieRepository.toggleWatchLater(movie)
                    sendEffect(WatchlistEffect.ShowMessage("Removed '${movie.title}' from Watch Later"))
                }
            } catch (e: Exception) {
                sendEffect(WatchlistEffect.ShowMessage("Failed to remove: ${e.message}"))
            }
        }
    }
}
