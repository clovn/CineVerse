package com.cineverse.data.mapper

import com.cineverse.core.database.WatchlistMovie
import com.cineverse.data.remote.model.CastMemberDto
import com.cineverse.data.remote.model.GenreDto
import com.cineverse.data.remote.model.MovieDetailsResponse
import com.cineverse.data.remote.model.MovieDto
import com.cineverse.domain.model.CastMember
import com.cineverse.domain.model.Genre
import com.cineverse.domain.model.Movie
import com.cineverse.domain.model.MovieDetails

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        posterPath = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage
    )
}

fun MovieDetailsResponse.toDomain(): MovieDetails {
    return MovieDetails(
        id = id,
        title = title,
        posterPath = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        backdropPath = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" },
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage,
        overview = overview ?: "",
        genres = genres.map { it.toDomain() },
        runtime = runtime ?: 0
    )
}

fun GenreDto.toDomain(): Genre {
    return Genre(id = id, name = name)
}

fun CastMemberDto.toDomain(): CastMember {
    return CastMember(
        id = id,
        name = name,
        character = character,
        profilePath = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
    )
}

fun WatchlistMovie.toDomain(): Movie {
    return Movie(
        id = id.toInt(),
        title = title,
        posterPath = posterPath,
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage
    )
}
