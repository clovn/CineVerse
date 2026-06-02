package com.cineverse.domain.model

data class UserProfile(
    val username: String
)

data class ProfileStats(
    val moviesWatched: Int,
    val favoriteGenres: List<String>
)
