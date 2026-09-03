package com.movieapp.features.movielist

import com.google.gson.annotations.SerializedName

/**
 * Encapsulates a single movie or television title returned in catalog feeds or search results.
 *
 * @property id Unique identifier.
 * @property title The display title.
 * @property slug URL-friendly slug used for detail routes.
 * @property poster The poster image URL.
 * @property releaseYear The year of release.
 * @property rating The score/rating out of 10.
 * @property mediaType Indicates whether the title is a movie or TV show.
 */
data class MovieDTO(
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

    @SerializedName("release_year", alternate = ["year", "first_air_date"])
    val releaseYear: String? = null,

    @SerializedName("rating", alternate = ["vote_average", "score"])
    val rating: Double? = null,

    @SerializedName("type", alternate = ["media_type"])
    val mediaType: String? = null
) {
    /**
     * Resolves the primary title cleanly, falling back to nameFallback if title is null.
     */
    val displayTitle: String
        get() = title ?: nameFallback ?: "Untitled"

    /**
     * Formats rating as a clean single-decimal string (e.g. "8.5").
     */
    val formattedRating: String
        get() = rating?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "N/A"

    /**
     * Resolves the 4-digit release year safely.
     */
    val displayYear: String
        get() = releaseYear?.take(4) ?: "Unknown"
}
