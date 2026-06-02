package com.cineverse.domain.repository

import com.cineverse.domain.model.CastMember
import com.cineverse.domain.model.Movie
import com.cineverse.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getNowPlaying(): List<Movie>
    suspend fun getTrending(): List<Movie>
    suspend fun searchMovies(query: String, genreId: Int?): List<Movie>
    suspend fun getMovieDetails(id: Int): MovieDetails
    suspend fun getMovieCast(id: Int): List<CastMember>
    suspend fun getRandomMovie(): Movie
    
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun getWatchLaterMovies(): Flow<List<Movie>>
    suspend fun isFavorite(movieId: Int): Boolean
    suspend fun isWatchLater(movieId: Int): Boolean
    suspend fun toggleFavorite(movie: Movie)
    suspend fun toggleWatchLater(movie: Movie)
    suspend fun removeMovie(movieId: Int)
}
