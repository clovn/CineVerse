package com.cineverse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.cineverse.core.database.CineVerseDatabase
import com.cineverse.data.mapper.toDomain
import com.cineverse.data.remote.model.MovieCreditsResponse
import com.cineverse.data.remote.model.MovieDetailsResponse
import com.cineverse.data.remote.model.MovieListResponse
import com.cineverse.domain.model.CastMember
import com.cineverse.domain.model.Genre
import com.cineverse.domain.model.Movie
import com.cineverse.domain.model.MovieDetails
import com.cineverse.domain.repository.MovieRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MovieRepositoryImpl(
    private val httpClient: HttpClient,
    private val database: CineVerseDatabase
) : MovieRepository {

    private val queries = database.cineVerseDatabaseQueries

    override suspend fun getNowPlaying(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val response: MovieListResponse = httpClient.get("movie/now_playing").body()
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            println("[MovieRepository] getNowPlaying network error, loading local fallback: ${e.message}")
            fallbackMovies().shuffled()
        }
    }

    override suspend fun getTrending(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val response: MovieListResponse = httpClient.get("trending/movie/week").body()
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            println("[MovieRepository] getTrending network error, loading local fallback: ${e.message}")
            fallbackMovies()
        }
    }

    override suspend fun searchMovies(query: String, genreId: Int?): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val response: MovieListResponse = httpClient.get("search/movie") {
                parameter("query", query)
            }.body()
            val results = response.results.map { it.toDomain() }
            if (genreId != null) {

                results
            } else {
                results
            }
        } catch (e: Exception) {
            println("[MovieRepository] searchMovies network error, loading local fallback: ${e.message}")
            fallbackMovies().filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun getMovieDetails(id: Int): MovieDetails = withContext(Dispatchers.IO) {
        try {
            val response: MovieDetailsResponse = httpClient.get("movie/$id").body()
            response.toDomain()
        } catch (e: Exception) {
            println("[MovieRepository] getMovieDetails network error, loading local fallback: ${e.message}")
            val fallback = fallbackMovies().firstOrNull { it.id == id } ?: fallbackMovies().first()
            MovieDetails(
                id = fallback.id,
                title = fallback.title,
                posterPath = fallback.posterPath,
                backdropPath = fallback.posterPath, 
                releaseDate = fallback.releaseDate,
                voteAverage = fallback.voteAverage,
                overview = "This is a detailed cinematic synopsis for '${fallback.title}'. Immersive storytelling with stellar performances, showcasing stunning visuals and breathtaking audio production in high definition.",
                genres = listOf(Genre(28, "Action"), Genre(12, "Adventure"), Genre(878, "Sci-Fi")),
                runtime = 148
            )
        }
    }

    override suspend fun getMovieCast(id: Int): List<CastMember> = withContext(Dispatchers.IO) {
        try {
            val response: MovieCreditsResponse = httpClient.get("movie/$id/credits").body()
            response.cast.map { it.toDomain() }
        } catch (e: Exception) {
            println("[MovieRepository] getMovieCast network error, loading local fallback: ${e.message}")
            listOf(
                CastMember(101, "Timothée Chalamet", "Paul Atreides", "https://image.tmdb.org/t/p/w185/BE3S4tLI6HuuSy.jpg"),
                CastMember(102, "Zendaya", "Chani", "https://image.tmdb.org/t/p/w185/8VtB7vJD211pf3H3S.jpg"),
                CastMember(103, "Cillian Murphy", "J. Robert Oppenheimer", "https://image.tmdb.org/t/p/w185/8Gxv2Z7HqD619.jpg"),
                CastMember(104, "Florence Pugh", "Jean Tatlock", "https://image.tmdb.org/t/p/w185/gEU2QvH353eRP.jpg")
            )
        }
    }

    override suspend fun getRandomMovie(): Movie = withContext(Dispatchers.IO) {
        val movies = try {
            val response: MovieListResponse = httpClient.get("trending/movie/week").body()
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            fallbackMovies()
        }
        movies[Random.nextInt(movies.size)]
    }

    private fun getCurrentUsername(): String {
        return queries.getSession().executeAsOneOrNull()?.currentUsername ?: ""
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        val username = getCurrentUsername()
        return queries.getFavoriteMovies(username)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getWatchLaterMovies(): Flow<List<Movie>> {
        val username = getCurrentUsername()
        return queries.getWatchLaterMovies(username)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun isFavorite(movieId: Int): Boolean = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        queries.getMovieById(movieId.toLong(), username).executeAsOneOrNull()?.isFavorite == 1L
    }

    override suspend fun isWatchLater(movieId: Int): Boolean = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        queries.getMovieById(movieId.toLong(), username).executeAsOneOrNull()?.isWatchLater == 1L
    }

    override suspend fun toggleFavorite(movie: Movie) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        val existing = queries.getMovieById(movie.id.toLong(), username).executeAsOneOrNull()
        val isFavoriteNow = if (existing?.isFavorite == 1L) 0L else 1L
        val isWatchLaterVal = existing?.isWatchLater ?: 0L
        
        if (isFavoriteNow == 0L && isWatchLaterVal == 0L) {
            queries.removeMovie(movie.id.toLong(), username)
        } else {
            queries.insertOrReplaceMovie(
                id = movie.id.toLong(),
                username = username,
                title = movie.title,
                posterPath = movie.posterPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                isFavorite = isFavoriteNow,
                isWatchLater = isWatchLaterVal
            )
        }
    }

    override suspend fun toggleWatchLater(movie: Movie) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        val existing = queries.getMovieById(movie.id.toLong(), username).executeAsOneOrNull()
        val isWatchLaterNow = if (existing?.isWatchLater == 1L) 0L else 1L
        val isFavoriteVal = existing?.isFavorite ?: 0L
        
        if (isWatchLaterNow == 0L && isFavoriteVal == 0L) {
            queries.removeMovie(movie.id.toLong(), username)
        } else {
            queries.insertOrReplaceMovie(
                id = movie.id.toLong(),
                username = username,
                title = movie.title,
                posterPath = movie.posterPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                isFavorite = isFavoriteVal,
                isWatchLater = isWatchLaterNow
            )
        }
    }

    override suspend fun removeMovie(movieId: Int) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        queries.removeMovie(movieId.toLong(), username)
    }

    override suspend fun getMovieNote(movieId: Int, username: String): String? = withContext(Dispatchers.IO) {
        queries.getNote(movieId.toLong(), username).executeAsOneOrNull()?.noteText
    }

    override suspend fun saveMovieNote(movieId: Int, username: String, noteText: String) = withContext(Dispatchers.IO) {
        queries.insertOrReplaceNote(movieId.toLong(), username, noteText)
    }

    override suspend fun deleteMovieNote(movieId: Int, username: String) = withContext(Dispatchers.IO) {
        queries.deleteNote(movieId.toLong(), username)
    }

    private fun fallbackMovies(): List<Movie> = listOf(
        Movie(693134, "Dune: Part Two", "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", "2024-02-27", 8.3),
        Movie(872585, "Oppenheimer", "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", "2023-07-19", 8.1),
        Movie(569094, "Spider-Man: Across the Spider-Verse", "https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg", "2023-05-31", 8.4),
        Movie(157336, "Interstellar", "https://image.tmdb.org/t/p/w500/yQvGrMoipbRoddT0ZR8tPoR7NfX.jpg", "2014-11-05", 8.6),
        Movie(27205, "Inception", "https://image.tmdb.org/t/p/w500/xlaY2zyzMfkhk0HSC5VUwzoZPU1.jpg", "2010-07-15", 8.3),
        Movie(155, "The Dark Knight", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg", "2008-07-16", 8.5)
    )
}
