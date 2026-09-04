package com.movieapp.util

/**
 * Global constants for external API endpoints, image assets, and configuration defaults.
 */
object Constants {
    /**
     * Base URL for the external movie and TV show REST API loaded from BuildConfig.
     */
    val BASE_URL: String = com.movieapp.BuildConfig.MOVIE_API_URL

    /**
     * Connection and socket timeout in seconds.
     */
    const val NETWORK_TIMEOUT_SECONDS = 30L

    /**
     * User-Agent header string to prevent bot challenge drops.
     */
    const val USER_AGENT = "MovieApp/1.0 (Android; Linux; Android 14)"
}

