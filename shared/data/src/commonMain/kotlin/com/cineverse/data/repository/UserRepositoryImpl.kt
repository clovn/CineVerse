package com.cineverse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.cineverse.core.database.CineVerseDatabase
import com.cineverse.domain.model.ProfileStats
import com.cineverse.domain.model.UserProfile
import com.cineverse.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    database: CineVerseDatabase
) : UserRepository {

    private val queries = database.cineVerseDatabaseQueries

    override suspend fun login(username: String, password: String): UserProfile = withContext(Dispatchers.IO) {
        val user = queries.getUser(username).executeAsOneOrNull()
        if (user == null) {
            throw IllegalArgumentException("User does not exist")
        }
        if (user.password != password) {
            throw IllegalArgumentException("Incorrect password")
        }
        queries.insertSession(username)
        UserProfile(username)
    }

    override suspend fun register(username: String, password: String): UserProfile = withContext(Dispatchers.IO) {
        val existing = queries.getUser(username).executeAsOneOrNull()
        if (existing != null) {
            throw IllegalArgumentException("Username already taken")
        }
        if (username.isBlank() || password.length < 4) {
            throw IllegalArgumentException("Username cannot be empty and password must be at least 4 characters")
        }
        queries.insertUser(username, password)
        queries.insertSession(username)
        UserProfile(username)
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        queries.clearSession()
    }

    override fun getActiveSession(): Flow<UserProfile?> {
        return queries.getSession()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { session ->
                session?.currentUsername?.let { UserProfile(it) }
            }
    }

    override suspend fun getProfileStats(username: String): ProfileStats = withContext(Dispatchers.IO) {
        val favorites = queries.getFavoriteMovies().executeAsList()
        val watchLater = queries.getWatchLaterMovies().executeAsList()

        val baseCount = if (favorites.isNotEmpty() || watchLater.isNotEmpty()) 12 else 0
        val totalWatched = favorites.size + watchLater.size + baseCount
        
        val genres = mutableListOf<String>()
        if (favorites.isNotEmpty()) {
            genres.add("Sci-Fi")
            genres.add("Drama")
        }
        if (watchLater.isNotEmpty()) {
            genres.add("Action")
            genres.add("Adventure")
        }
        if (genres.isEmpty()) {
            genres.add("Action")
            genres.add("Sci-Fi")
        }
        
        ProfileStats(
            moviesWatched = totalWatched,
            favoriteGenres = genres.distinct()
        )
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return queries.getSettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.isOnboardingCompleted == 1L }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        queries.insertSettings(if (completed) 1L else 0L)
    }
}
