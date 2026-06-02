package com.cineverse.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val releaseDate: String,
    val voteAverage: Double
)

data class MovieDetails(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val overview: String,
    val genres: List<Genre>,
    val runtime: Int
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?
)

data class Genre(
    val id: Int,
    val name: String
)
