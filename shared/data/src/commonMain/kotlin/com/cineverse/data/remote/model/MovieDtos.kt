package com.cineverse.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieListResponse(
    @SerialName("results") val results: List<MovieDto>
)

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0
)

@Serializable
data class MovieDetailsResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("overview") val overview: String? = "",
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("runtime") val runtime: Int? = 0
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
data class MovieCreditsResponse(
    @SerialName("cast") val cast: List<CastMemberDto>
)

@Serializable
data class CastMemberDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("character") val character: String,
    @SerialName("profile_path") val profilePath: String? = null
)
