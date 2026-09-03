package com.movieapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.movieapp.features.movielist.MediaCategory
import com.movieapp.features.movielist.MovieDTO
import com.movieapp.features.movielist.MovieListRepository
import com.movieapp.features.movielist.MovieListResponseDTO
import com.movieapp.features.movielist.MovieListScreen
import com.movieapp.features.movielist.MovieListViewModel
import com.movieapp.network.MovieApiService
import com.movieapp.theme.MovieAppTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-End Compose test suite for the Catalog Feed & Category Tabs flow.
 * Runs on host JVM without requiring an emulator via Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FeedE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeMovies = listOf(
        MovieDTO(id = 1L, title = "Inception", slug = "inception", releaseYear = "2010", rating = 8.8, mediaType = "movie"),
        MovieDTO(id = 2L, title = "Interstellar", slug = "interstellar", releaseYear = "2014", rating = 8.7, mediaType = "movie")
    )

    private val fakeTvShows = listOf(
        MovieDTO(id = 101L, title = "Breaking Bad", slug = "breaking-bad", releaseYear = "2008", rating = 9.5, mediaType = "tv"),
        MovieDTO(id = 102L, title = "Chernobyl", slug = "chernobyl", releaseYear = "2019", rating = 9.4, mediaType = "tv")
    )

    private val fakeApiService = object : MovieApiService {
        override suspend fun getMovies(page: Int): MovieListResponseDTO {
            return if (page == 1) {
                MovieListResponseDTO(items = fakeMovies, currentPage = 1, totalPages = 1)
            } else {
                MovieListResponseDTO(items = emptyList(), currentPage = page, totalPages = 1)
            }
        }
        override suspend fun getTvShows(page: Int): MovieListResponseDTO {
            return if (page == 1) {
                MovieListResponseDTO(items = fakeTvShows, currentPage = 1, totalPages = 1)
            } else {
                MovieListResponseDTO(items = emptyList(), currentPage = page, totalPages = 1)
            }
        }
        override suspend fun getMovieDetail(slug: String) = throw UnsupportedOperationException()
        override suspend fun getTvShowDetail(slug: String) = throw UnsupportedOperationException()
        override suspend fun searchTitles(keyword: String, page: Int) = throw UnsupportedOperationException()
    }

    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
    private val repository = MovieListRepository(fakeApiService, testDispatcher)

    @Test
    fun feedDisplaysMoviesInitiallyAndSwitchesToTvShows() {
        val viewModel = MovieListViewModel(repository)
        var clickedSlug: String? = null
        var clickedIsTv: Boolean? = null

        composeTestRule.setContent {
            MovieAppTheme {
                MovieListScreen(
                    viewModel = viewModel,
                    onTitleClick = { slug, isTv ->
                        clickedSlug = slug
                        clickedIsTv = isTv
                    }
                )
            }
        }

        // Verify initial Movies list
        composeTestRule.onNodeWithText("Movies").assertIsDisplayed()
        composeTestRule.onNodeWithText("TV Shows").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()
        composeTestRule.onNodeWithText("Interstellar").assertIsDisplayed()

        // Switch to TV Shows tab
        composeTestRule.onNodeWithText("TV Shows").performClick()

        // Verify TV shows are displayed
        composeTestRule.onNodeWithText("Breaking Bad").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chernobyl").assertIsDisplayed()

        // Click on a TV show card
        composeTestRule.onNodeWithText("Breaking Bad").performClick()
        assertEquals("breaking-bad", clickedSlug)
        assertTrue(clickedIsTv == true)
    }

    @Test
    fun pullToRefreshTriggersReloadSuccessfully() {
        val viewModel = MovieListViewModel(repository)

        composeTestRule.setContent {
            MovieAppTheme {
                MovieListScreen(
                    viewModel = viewModel,
                    onTitleClick = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()

        // Trigger refresh directly on ViewModel
        viewModel.refresh()
        composeTestRule.waitForIdle()

        // Verify content remains loaded cleanly
        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()
    }
}
