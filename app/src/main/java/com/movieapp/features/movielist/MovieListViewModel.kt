package com.movieapp.features.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Supported media categories.
 */
enum class MediaCategory {
    MOVIES,
    TV_SHOWS
}

/**
 * Immutable UI state for the media feed.
 */
data class MovieListUiState(
    val activeCategory: MediaCategory = MediaCategory.MOVIES,
    val movies: List<MovieDTO> = emptyList(),
    val tvShows: List<MovieDTO> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val moviesPage: Int = 1,
    val tvShowsPage: Int = 1,
    val moviesHasMore: Boolean = true,
    val tvShowsHasMore: Boolean = true
) {
    val currentDisplayList: List<MovieDTO>
        get() = if (activeCategory == MediaCategory.MOVIES) movies else tvShows

    val currentHasMore: Boolean
        get() = if (activeCategory == MediaCategory.MOVIES) moviesHasMore else tvShowsHasMore
}

/**
 * State holder managing catalog feeds, tab switches, and continuous pagination.
 */
class MovieListViewModel(
    private val repository: MovieListRepository = MovieListRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init {
        loadInitialFeeds()
    }

    private fun loadInitialFeeds() {
        fetchMoviesPage(1)
        fetchTvShowsPage(1)
    }

    /**
     * Pull-to-refresh: resets page to 1 and reloads current active category.
     */
    fun refresh() {
        val state = _uiState.value
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        if (state.activeCategory == MediaCategory.MOVIES) {
            fetchMoviesPage(1, isRefresh = true)
        } else {
            fetchTvShowsPage(1, isRefresh = true)
        }
    }

    /**
     * Toggles between Movies and TV Shows.
     * Retains already-loaded data in-memory without duplicate network requests.
     */
    fun selectCategory(category: MediaCategory) {
        if (_uiState.value.activeCategory != category) {
            _uiState.update { it.copy(activeCategory = category, errorMessage = null) }
        }
    }

    /**
     * Loads the next page of items for the currently active category.
     */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isPaginating || !state.currentHasMore) return

        if (state.activeCategory == MediaCategory.MOVIES) {
            fetchMoviesPage(state.moviesPage + 1)
        } else {
            fetchTvShowsPage(state.tvShowsPage + 1)
        }
    }

    /**
     * Retries loading after an error.
     */
    fun retry() {
        val state = _uiState.value
        if (state.activeCategory == MediaCategory.MOVIES) {
            fetchMoviesPage(if (state.movies.isEmpty()) 1 else state.moviesPage + 1)
        } else {
            fetchTvShowsPage(if (state.tvShows.isEmpty()) 1 else state.tvShowsPage + 1)
        }
    }

    private fun fetchMoviesPage(page: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.getMovies(page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                            else if (page == 1 && it.movies.isEmpty()) it.copy(isInitialLoading = true, errorMessage = null)
                            else it.copy(isPaginating = true, errorMessage = null)
                        }
                    }
                    is Resource.Success -> {
                        val response = resource.data
                        val newItems = response?.safeItems ?: emptyList()
                        _uiState.update { current ->
                            val combined = if (page == 1 || isRefresh) {
                                newItems
                            } else {
                                (current.movies + newItems).distinctBy { "${it.id}_${it.slug}" }
                            }
                            current.copy(
                                movies = combined,
                                moviesPage = page,
                                moviesHasMore = response?.canLoadMore ?: (newItems.isNotEmpty()),
                                isInitialLoading = false,
                                isPaginating = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                isPaginating = false,
                                isRefreshing = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchTvShowsPage(page: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.getTvShows(page).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                            else if (page == 1 && it.tvShows.isEmpty()) it.copy(isInitialLoading = true, errorMessage = null)
                            else it.copy(isPaginating = true, errorMessage = null)
                        }
                    }
                    is Resource.Success -> {
                        val response = resource.data
                        val newItems = response?.safeItems ?: emptyList()
                        _uiState.update { current ->
                            val combined = if (page == 1 || isRefresh) {
                                newItems
                            } else {
                                (current.tvShows + newItems).distinctBy { "${it.id}_${it.slug}" }
                            }
                            current.copy(
                                tvShows = combined,
                                tvShowsPage = page,
                                tvShowsHasMore = response?.canLoadMore ?: (newItems.isNotEmpty()),
                                isInitialLoading = false,
                                isPaginating = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isInitialLoading = false,
                                isPaginating = false,
                                isRefreshing = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }
}
