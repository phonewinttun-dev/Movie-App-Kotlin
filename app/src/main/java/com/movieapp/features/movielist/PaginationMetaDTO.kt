package com.movieapp.features.movielist

import com.google.gson.annotations.SerializedName

/**
 * PaginationMetaDTO encapsulates the pagination metadata returned in API list and search responses.
 *
 * It maps the nested "meta" object containing the current page index, last page index,
 * and total records count to coordinate infinite scroll pagination across catalog feeds.
 *
 * @property currentPage The 1-based page number currently returned.
 * @property lastPage The total number of pages available.
 * @property perPage The number of items per page.
 * @property total The total number of items available across all pages.
 */
data class PaginationMetaDTO(
    @SerializedName("current_page")
    val currentPage: Int = 1,

    @SerializedName("last_page")
    val lastPage: Int = 1,

    @SerializedName("per_page")
    val perPage: Int = 20,

    @SerializedName("total")
    val total: Int = 0
)
