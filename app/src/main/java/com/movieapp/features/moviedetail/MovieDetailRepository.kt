package com.movieapp.features.moviedetail

import com.movieapp.network.MovieApiService
import com.movieapp.network.NetworkClient
import com.movieapp.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Single source of truth for title metadata, storyline, and TV season details.
 */
class MovieDetailRepository(
    private val apiService: MovieApiService = NetworkClient.apiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Retrieves full detail metadata for a movie by slug.
     */
    fun getMovieDetail(slug: String): Flow<Resource<MovieDetailDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMovieDetail(slug)
            val data = response.data
            if (data != null) {
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error("Movie details aren't available."))
            }
        } catch (e: Exception) {
            android.util.Log.e("MovieDetailRepository", "Failed to fetch movie detail: ${e.javaClass.simpleName}: ${e.message}", e)
            emit(Resource.Error("Couldn't load movie details. Check your connection and try again."))
        }
    }.flowOn(ioDispatcher)

    /**
     * Retrieves full detail metadata for a TV show by slug.
     */
    fun getTvShowDetail(slug: String): Flow<Resource<MovieDetailDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTvShowDetail(slug)
            val data = response.data
            if (data != null) {
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error("Show details aren't available."))
            }
        } catch (e: Exception) {
            android.util.Log.e("MovieDetailRepository", "Failed to fetch TV show detail: ${e.javaClass.simpleName}: ${e.message}", e)
            emit(Resource.Error("Couldn't load show details. Check your connection and try again."))
        }
    }.flowOn(ioDispatcher)
}
