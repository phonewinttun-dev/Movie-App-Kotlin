package com.movieapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.movieapp.data.local.AppDatabase
import com.movieapp.data.local.MovieDao
import com.movieapp.data.local.MovieEntity
import com.movieapp.features.downloadlinks.DownloadManagerHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DownloadAndBookmarkUnitTest {

    private lateinit var database: AppDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        movieDao = database.movieDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testTelegramDeepLinkConversion() {
        // Standard channel message link
        val webUrl1 = "https://t.me/ch003agwpcd/881"
        val tgLink1 = DownloadManagerHelper.convertToTelegramDeepLink(webUrl1)
        assertEquals("tg://resolve?domain=ch003agwpcd&post=881", tgLink1)

        // Telegram link without https prefix
        val webUrl2 = "t.me/my_channel/42"
        val tgLink2 = DownloadManagerHelper.convertToTelegramDeepLink(webUrl2)
        assertEquals("tg://resolve?domain=my_channel&post=42", tgLink2)

        // General channel link without post
        val webUrl3 = "https://t.me/my_channel"
        val tgLink3 = DownloadManagerHelper.convertToTelegramDeepLink(webUrl3)
        assertEquals("tg://resolve?domain=my_channel", tgLink3)
    }

    @Test
    fun testRoomCacheAndBookmarkOperations() = runBlocking {
        val sampleMovie = MovieEntity(
            slug = "mayday-2026",
            title = "Mayday",
            poster = "https://example.com/poster.jpg",
            rating = "7.2",
            releaseYear = "2026",
            isTvShow = false,
            plot = "A great adventure movie.",
            jsonDetail = "{}",
            isBookmarked = false,
            bookmarkedAt = 0L
        )

        // Insert cached entity
        movieDao.insertOrUpdate(sampleMovie)
        val retrieved = movieDao.getMovieBySlug("mayday-2026")
        assertNotNull(retrieved)
        assertEquals("Mayday", retrieved?.title)
        assertFalse(retrieved?.isBookmarked ?: true)

        // Bookmark the movie
        val timestamp = System.currentTimeMillis()
        movieDao.updateBookmarkStatus("mayday-2026", true, timestamp)

        // Verify bookmark query
        val isBookmarked = movieDao.isBookmarked("mayday-2026").first()
        assertTrue(isBookmarked == true)

        val bookmarks = movieDao.getBookmarkedMovies().first()
        assertEquals(1, bookmarks.size)
        assertEquals("mayday-2026", bookmarks[0].slug)

        // Unbookmark
        movieDao.updateBookmarkStatus("mayday-2026", false, 0L)
        val unbookmarkedList = movieDao.getBookmarkedMovies().first()
        assertTrue(unbookmarkedList.isEmpty())
    }
}
