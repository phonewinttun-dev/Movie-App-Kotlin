package com.movieapp.features.moviedetail

import com.movieapp.network.MovieApiService
import com.movieapp.network.NetworkClient
import com.movieapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Single source of truth for title metadata, storyline, and TV season details.
 */
class MovieDetailRepository(
    private val apiService: MovieApiService = NetworkClient.apiService
) {

    /**
     * Retrieves full detail metadata for a movie by slug.
     */
    fun getMovieDetail(slug: String): Flow<Resource<MovieDetailDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMovieDetail(slug)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error("Unable to load movie details right now. Please check your connection and try again."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Retrieves full detail metadata for a TV show by slug.
     */
    fun getTvShowDetail(slug: String): Flow<Resource<MovieDetailDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTvShowDetail(slug)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error("Unable to load series details right now. Please check your connection and try again."))
        }
    }.flowOn(Dispatchers.IO)
}
