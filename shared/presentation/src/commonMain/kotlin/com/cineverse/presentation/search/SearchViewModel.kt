package com.cineverse.presentation.search

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.Genre
import com.cineverse.domain.model.Movie
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: List<Movie> = emptyList(),
    val isSearching: Boolean = false,
    val selectedGenre: Genre? = null,
    val availableGenres: List<Genre> = listOf(
        Genre(28, "Action"),
        Genre(12, "Adventure"),
        Genre(35, "Comedy"),
        Genre(18, "Drama"),
        Genre(878, "Sci-Fi"),
        Genre(53, "Thriller")
    )
)

sealed class SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent()
    data class ApplyFilter(val genre: Genre?) : SearchIntent()
}

sealed class SearchEffect {
    data class ShowToast(val message: String) : SearchEffect()
}

class SearchViewModel(
    private val movieRepository: MovieRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<SearchState, SearchIntent, SearchEffect>(SearchState()) {

    private var searchJob: Job? = null

    init {
        analyticsTracker.trackEvent("launch_search")
        executeSearch("")
    }

    override fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                updateState { it.copy(query = intent.query) }
                // Debounce search requests to prevent API rate limiting
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(300)
                    executeSearch(intent.query)
                }
            }
            is SearchIntent.ApplyFilter -> {
                updateState { it.copy(selectedGenre = intent.genre) }
                executeSearch(state.value.query)
            }
        }
    }

    private fun executeSearch(query: String) {
        viewModelScope.launch {
            updateState { it.copy(isSearching = true) }
            try {
                val searchResults = movieRepository.searchMovies(query, state.value.selectedGenre?.id)
                updateState { it.copy(results = searchResults, isSearching = false) }
            } catch (e: Exception) {
                updateState { it.copy(isSearching = false) }
                sendEffect(SearchEffect.ShowToast(e.message ?: "Search failed"))
            }
        }
    }
}
