package com.cineverse.presentation.details

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.CastMember
import com.cineverse.domain.model.Movie
import com.cineverse.domain.model.MovieDetails
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.launch

data class DetailsState(
    val movieDetails: MovieDetails? = null,
    val cast: List<CastMember> = emptyList(),
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false,
    val error: String? = null
)

sealed class DetailsIntent {
    data class LoadDetails(val id: Int) : DetailsIntent()
    data object ToggleFavorite : DetailsIntent()
    data object ToggleWatchLater : DetailsIntent()
    data class ScheduleReminder(val dateTime: String) : DetailsIntent()
}

sealed class DetailsEffect {
    data class ScheduleNotification(val movieTitle: String, val releaseDate: String) : DetailsEffect()
    data class ShowMessage(val message: String) : DetailsEffect()
}

class DetailsViewModel(
    private val movieRepository: MovieRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<DetailsState, DetailsIntent, DetailsEffect>(DetailsState()) {

    override fun handleIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.LoadDetails -> loadDetails(intent.id)
            is DetailsIntent.ToggleFavorite -> toggleFavorite()
            is DetailsIntent.ToggleWatchLater -> toggleWatchLater()
            is DetailsIntent.ScheduleReminder -> scheduleReminder(intent.dateTime)
        }
    }

    private fun loadDetails(id: Int) {
        viewModelScope.launch {
            analyticsTracker.trackEvent("launch_details", mapOf("movie_id" to id.toString()))
            updateState { it.copy(isLoading = true, error = null) }
            try {
                val details = movieRepository.getMovieDetails(id)
                val cast = movieRepository.getMovieCast(id)
                val fav = movieRepository.isFavorite(id)
                val watch = movieRepository.isWatchLater(id)
                updateState {
                    it.copy(
                        movieDetails = details,
                        cast = cast,
                        isFavorite = fav,
                        isWatchLater = watch,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                sendEffect(DetailsEffect.ShowMessage(e.message ?: "Failed to load movie details"))
            }
        }
    }

    private fun toggleFavorite() {
        val details = state.value.movieDetails ?: return
        viewModelScope.launch {
            val movie = Movie(
                id = details.id,
                title = details.title,
                posterPath = details.posterPath,
                releaseDate = details.releaseDate,
                voteAverage = details.voteAverage
            )
            movieRepository.toggleFavorite(movie)
            val isFav = movieRepository.isFavorite(details.id)
            updateState { it.copy(isFavorite = isFav) }
            sendEffect(
                DetailsEffect.ShowMessage(
                    if (isFav) "Added to Favorites" else "Removed from Favorites"
                )
            )
        }
    }

    private fun toggleWatchLater() {
        val details = state.value.movieDetails ?: return
        viewModelScope.launch {
            val movie = Movie(
                id = details.id,
                title = details.title,
                posterPath = details.posterPath,
                releaseDate = details.releaseDate,
                voteAverage = details.voteAverage
            )
            movieRepository.toggleWatchLater(movie)
            val isWatch = movieRepository.isWatchLater(details.id)
            updateState { it.copy(isWatchLater = isWatch) }
            sendEffect(
                DetailsEffect.ShowMessage(
                    if (isWatch) "Added to Watch Later" else "Removed from Watch Later"
                )
            )
        }
    }

    private fun scheduleReminder(dateTime: String) {
        val details = state.value.movieDetails ?: return
        sendEffect(DetailsEffect.ScheduleNotification(details.title, dateTime))
    }
}
