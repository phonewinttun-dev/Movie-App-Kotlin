package com.movieapp.features.movielist

import com.movieapp.network.MovieApiService
import com.movieapp.network.NetworkClient
import com.movieapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Single source of truth for retrieving movie and TV show catalog feeds.
 */
class MovieListRepository(
    private val apiService: MovieApiService = NetworkClient.apiService
) {

    /**
     * Retrieves a page of movies from the remote service.
     */
    fun getMovies(page: Int): Flow<Resource<MovieListResponseDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMovies(page)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error("Unable to load movies right now. Please check your internet connection and try again."))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Retrieves a page of television shows from the remote service.
     */
    fun getTvShows(page: Int): Flow<Resource<MovieListResponseDTO>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTvShows(page)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error("Unable to load TV shows right now. Please check your internet connection and try again."))
        }
    }.flowOn(Dispatchers.IO)
}
