package com.movieapp.features.search

import com.google.gson.annotations.SerializedName
import com.movieapp.features.movielist.MovieDTO

/**
 * Encapsulates search results matching a user keyword query.
 *
 * @property items The matching movies and television series.
 * @property currentPage The current result page.
 * @property totalPages Total available pages for the search query.
 */
data class SearchResponseDTO(
    @SerializedName("data", alternate = ["results", "items"])
    val items: List<MovieDTO>? = null,

    @SerializedName("page", alternate = ["current_page"])
    val currentPage: Int? = null,

    @SerializedName("total_pages", alternate = ["last_page"])
    val totalPages: Int? = null
) {
    val safeItems: List<MovieDTO>
        get() = items ?: emptyList()
}
