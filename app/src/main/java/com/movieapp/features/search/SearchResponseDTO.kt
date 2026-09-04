package com.movieapp.features.search

import com.google.gson.annotations.SerializedName
import com.movieapp.features.movielist.MovieDTO
import com.movieapp.features.movielist.PaginationMetaDTO

/**
 * Encapsulates search results matching a user keyword query.
 *
 * @property items The matching movies and television series.
 * @property meta Nested pagination metadata.
 */
data class SearchResponseDTO(
    @SerializedName("data", alternate = ["results", "items"])
    val items: List<MovieDTO>? = null,

    @SerializedName("meta")
    val meta: PaginationMetaDTO? = null,

    @SerializedName("page", alternate = ["current_page"])
    private val rawCurrentPage: Int? = null,

    @SerializedName("total_pages", alternate = ["last_page"])
    private val rawTotalPages: Int? = null
) {
    val safeItems: List<MovieDTO>
        get() = items ?: emptyList()

    val currentPage: Int
        get() = meta?.currentPage ?: rawCurrentPage ?: 1

    val totalPages: Int
        get() = meta?.lastPage ?: rawTotalPages ?: 1

    val canLoadMore: Boolean
        get() = currentPage < totalPages
}
