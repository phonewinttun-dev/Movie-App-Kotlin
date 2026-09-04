package com.movieapp.network

import com.movieapp.features.moviedetail.MovieDetailResponseDTO
import com.movieapp.features.movielist.MovieListResponseDTO
import com.movieapp.features.search.SearchResponseDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Direct REST API contract for fetching movie and television data from HomieTV.
 */
interface MovieApiService {

    /**
     * Retrieves paginated movies for the catalog feed.
     */
    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int
    ): MovieListResponseDTO

    /**
     * Retrieves paginated television shows for the catalog feed.
     */
    @GET("tv-shows")
    suspend fun getTvShows(
        @Query("page") page: Int
    ): MovieListResponseDTO

    /**
     * Retrieves detail information for a specific movie by its slug.
     */
    @GET("movies/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): MovieDetailResponseDTO

    /**
     * Retrieves detail information for a specific television show by its slug.
     */
    @GET("tv-shows/{slug}")
    suspend fun getTvShowDetail(
        @Path("slug") slug: String
    ): MovieDetailResponseDTO

    /**
     * Searches for movies and television shows by user keyword.
     */
    @GET("search")
    suspend fun searchTitles(
        @Query("keyword") keyword: String,
        @Query("page") page: Int
    ): SearchResponseDTO
}
