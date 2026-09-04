package com.movieapp.features.moviedetail

import com.google.gson.annotations.SerializedName

/**
 * MovieDetailResponseDTO models the outer response envelope for movie and TV show detail endpoints.
 *
 * Both /api/movies/{slug} and /api/tv-shows/{slug} wrap the actual detail payload within
 * a top-level "data" object accompanied by operation success and message indicators.
 *
 * @property success Indicates whether the remote query succeeded.
 * @property message Remote status message (e.g. "Movie Details", "TV Show Details").
 * @property data The core metadata and download links for the requested title.
 */
data class MovieDetailResponseDTO(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: MovieDetailDTO? = null
)
