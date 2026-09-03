package com.movieapp.features.movielist

import com.google.gson.annotations.SerializedName

/**
 * Encapsulates a paginated list response from the movie or TV show feed endpoints.
 *
 * @property items List of titles returned for the current page.
 * @property currentPage The current page index.
 * @property totalPages Total number of pages available.
 * @property hasMore Indicates if more pages can be requested.
 */
data class MovieListResponseDTO(
    @SerializedName("data", alternate = ["results", "items"])
    val items: List<MovieDTO>? = null,

    @SerializedName("page", alternate = ["current_page"])
    val currentPage: Int? = null,

    @SerializedName("total_pages", alternate = ["last_page"])
    val totalPages: Int? = null,

    @SerializedName("has_more")
    val hasMore: Boolean? = null
) {
    /**
     * Resolves the list of titles safely, returning an empty list if null.
     */
    val safeItems: List<MovieDTO>
        get() = items ?: emptyList()

    /**
     * Determines whether additional pages are available to paginate.
     */
    val canLoadMore: Boolean
        get() = hasMore ?: ((currentPage ?: 1) < (totalPages ?: 1))
}
