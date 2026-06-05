package com.cineverse.presentation.dice

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.domain.model.Movie
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.presentation.base.BaseMviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DiceState(
    val randomMovie: Movie? = null,
    val isRolling: Boolean = false
)

sealed class DiceIntent {
    data object RollDice : DiceIntent()
}

sealed class DiceEffect {
    data class ShowSelection(val movie: Movie) : DiceEffect()
}

class DiceViewModel(
    private val movieRepository: MovieRepository,
    private val analyticsTracker: AnalyticsTracker
) : BaseMviViewModel<DiceState, DiceIntent, DiceEffect>(DiceState()) {

    init {
        analyticsTracker.trackEvent("launch_dice")
    }

    override fun handleIntent(intent: DiceIntent) {
        when (intent) {
            is DiceIntent.RollDice -> rollDice()
        }
    }

    private fun rollDice() {
        if (state.value.isRolling) return
        
        viewModelScope.launch {
            updateState { it.copy(isRolling = true, randomMovie = null) }
            
            delay(1500)
            try {
                val movie = movieRepository.getRandomMovie()
                updateState { it.copy(randomMovie = movie, isRolling = false) }
                sendEffect(DiceEffect.ShowSelection(movie))
            } catch (e: Exception) {
                updateState { it.copy(isRolling = false) }
            }
        }
    }
}
