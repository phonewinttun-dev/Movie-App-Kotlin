package com.movieapp.features.moviedetail

import com.google.gson.annotations.SerializedName

/**
 * Encapsulates full detail metadata for a single movie or television series.
 * Note: Download links are excluded from presentation.
 */
data class MovieDetailDTO(
    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("name")
    private val nameFallback: String? = null,

    @SerializedName("slug")
    val slug: String? = null,

    @SerializedName("poster", alternate = ["poster_path", "image"])
    val poster: String? = null,

    @SerializedName("backdrop", alternate = ["backdrop_path", "banner"])
    val backdrop: String? = null,

    @SerializedName("release_year", alternate = ["year", "first_air_date", "release_date"])
    val releaseYear: String? = null,

    @SerializedName("runtime", alternate = ["duration"])
    val runtime: String? = null,

    @SerializedName("rating", alternate = ["vote_average", "score"])
    val rating: Double? = null,

    @SerializedName("plot", alternate = ["overview", "description", "summary"])
    val plot: String? = null,

    @SerializedName("genres", alternate = ["genre_list"])
    val genres: List<String>? = null,

    @SerializedName("type", alternate = ["media_type"])
    val mediaType: String? = null,

    @SerializedName("seasons")
    val seasons: List<SeasonDTO>? = null
) {
    val displayTitle: String
        get() = title ?: nameFallback ?: "Untitled"

    val displayYear: String
        get() = releaseYear?.take(4) ?: "Unknown"

    val formattedRating: String
        get() = rating?.let { String.format(java.util.Locale.US, "%.1f / 10", it) } ?: "Not rated"

    val safeGenres: List<String>
        get() = genres ?: emptyList()

    val safeSeasons: List<SeasonDTO>
        get() = seasons ?: emptyList()
}

/**
 * Encapsulates a television show season.
 */
data class SeasonDTO(
    @SerializedName("season_number", alternate = ["number", "season"])
    val seasonNumber: Int = 1,

    @SerializedName("name", alternate = ["title"])
    val name: String? = null,

    @SerializedName("episodes")
    val episodes: List<EpisodeDTO>? = null
) {
    val safeEpisodes: List<EpisodeDTO>
        get() = episodes ?: emptyList()

    val displayName: String
        get() = name ?: "Season $seasonNumber"
}

/**
 * Encapsulates an individual television episode.
 */
data class EpisodeDTO(
    @SerializedName("episode_number", alternate = ["number", "episode"])
    val episodeNumber: Int = 1,

    @SerializedName("title", alternate = ["name"])
    val title: String? = null,

    @SerializedName("runtime", alternate = ["duration"])
    val runtime: String? = null
) {
    val displayTitle: String
        get() = title ?: "Episode $episodeNumber"
}
