package com.movieapp

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.movieapp.features.moviedetail.MovieDetailResponseDTO
import com.movieapp.features.movielist.MovieListResponseDTO
import com.movieapp.features.search.SearchResponseDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates Gson deserialization contracts against real-world HomieTV API JSON responses.
 */
class ApiContractTest {

    private val gson: Gson = GsonBuilder().setLenient().create()

    @Test
    fun testMovieListResponseDeserialization() {
        val json = """
            {
              "success": true,
              "message": "Movies All",
              "data": [
                {
                  "id": 25866,
                  "title": "Logan's War: Bound by Honor",
                  "slug": "logans-war-bound-by-honor-mtyitrey",
                  "year": "1998",
                  "poster": "https://media.homietv.com/poster.jpg",
                  "rating": "5.3",
                  "resolution": "1080p",
                  "categories": [
                    { "id": 1, "name": "Action" },
                    { "id": 2, "name": "Adventure" }
                  ],
                  "type": "movie"
                }
              ],
              "meta": {
                "current_page": 1,
                "from": 1,
                "last_page": 305,
                "per_page": 20,
                "total": 6092
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, MovieListResponseDTO::class.java)

        assertNotNull(response)
        assertEquals(1, response.safeItems.size)
        assertEquals("Logan's War: Bound by Honor", response.safeItems[0].displayTitle)
        assertEquals(1, response.currentPage)
        assertEquals(305, response.totalPages)
        assertTrue(response.canLoadMore)
        assertEquals(2, response.safeItems[0].categoryNames.size)
        assertEquals("Action", response.safeItems[0].categoryNames[0])
    }

    @Test
    fun testMovieDetailResponseWithDownloadLinks() {
        val json = """
            {
              "success": true,
              "message": "Movie Details",
              "data": {
                "id": 25866,
                "title": "Logan's War: Bound by Honor",
                "slug": "logans-war-bound-by-honor-mtyitrey",
                "year": "1998",
                "poster": "https://media.homietv.com/poster.jpg",
                "overview": "Logan fights to avenge his family.",
                "rating": "5.3",
                "categories": [
                  { "id": 1, "name": "Action" }
                ],
                "type": "movie",
                "movie_download_links": [
                  {
                    "id": 42136,
                    "server_name": "Megaup",
                    "url": "https://megaup.net/download/123",
                    "size": "1.84 GB",
                    "resolution": "1080p"
                  },
                  {
                    "id": 42137,
                    "server_name": "Telegram",
                    "url": "https://t.me/samplechannel",
                    "size": "700 MB",
                    "resolution": "720p"
                  }
                ]
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, MovieDetailResponseDTO::class.java)

        assertTrue(response.success)
        assertNotNull(response.data)
        val detail = response.data!!
        assertEquals("Logan's War: Bound by Honor", detail.displayTitle)
        assertEquals(2, detail.safeMovieDownloadLinks.size)
        assertEquals("Megaup", detail.safeMovieDownloadLinks[0].cleanServerName)
        assertFalse(detail.safeMovieDownloadLinks[0].isTelegram)
        assertTrue(detail.safeMovieDownloadLinks[1].isTelegram)
    }

    @Test
    fun testTvShowDetailResponseWithEpisodesAndLinks() {
        val json = """
            {
              "success": true,
              "message": "TV Show Details",
              "data": {
                "id": 5146,
                "title": "Ted Lasso",
                "slug": "ted-lasso-2vtovc1g",
                "year": "2020",
                "type": "tv-show",
                "seasons": [
                  {
                    "id": 9356,
                    "season_number": "1",
                    "season_name": "Season 1",
                    "episodes": [
                      {
                        "id": 106291,
                        "episode_number": "1",
                        "name": "Pilot",
                        "runtime": "30 Mins",
                        "tvshow_download_links": [
                          {
                            "id": 78560,
                            "server_name": "Telegram",
                            "url": "https://t.me/tedlasso",
                            "size": "350 MB",
                            "resolution": "720p"
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, MovieDetailResponseDTO::class.java)

        assertTrue(response.success)
        assertNotNull(response.data)
        val detail = response.data!!
        assertTrue(detail.isTvShow)
        assertEquals(1, detail.safeSeasons.size)
        val season1 = detail.safeSeasons[0]
        assertEquals("Season 1", season1.displayName)
        assertEquals(1, season1.safeEpisodes.size)
        val ep1 = season1.safeEpisodes[0]
        assertEquals("Ep 1: Pilot", ep1.displayTitle)
        assertEquals(1, ep1.safeDownloadLinks.size)
        assertTrue(ep1.safeDownloadLinks[0].isTelegram)
    }

    @Test
    fun testSearchResponseWithMixedTypes() {
        val json = """
            {
              "success": true,
              "message": "Search Results",
              "data": [
                {
                  "id": "25733",
                  "title": "Batman: Knightfall",
                  "slug": "batman-knightfall",
                  "year": "2026",
                  "rating": "7.2",
                  "type": "movie"
                },
                {
                  "id": "5132",
                  "title": "Batman: Caped Crusader",
                  "slug": "batman-caped-crusader",
                  "year": "2024",
                  "rating": "7.2",
                  "type": "tv-show"
                }
              ],
              "meta": {
                "current_page": 1,
                "last_page": 1,
                "total": 2
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, SearchResponseDTO::class.java)

        assertNotNull(response)
        assertEquals(2, response.safeItems.size)
        assertEquals(25733L, response.safeItems[0].id)
        assertEquals("Batman: Knightfall", response.safeItems[0].displayTitle)
        assertFalse(response.safeItems[0].isTvShow)
        assertTrue(response.safeItems[1].isTvShow)
        assertFalse(response.canLoadMore)
    }
}
