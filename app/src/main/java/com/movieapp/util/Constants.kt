package com.movieapp.util

/**
 * Global constants for external API endpoints, image assets, and configuration defaults.
 */
object Constants {
    /**
     * Base URL for the external movie REST API (The Movie Database / TMDB API v3).
     * All network requests are routed directly to this endpoint without an intermediate backend.
     */
    const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * Base URL for fetching poster and backdrop image assets directly from TMDB CDN.
     */
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    /**
     * Default public API key placeholder. Replace with your actual TMDB API key or inject via BuildConfig.
     */
    const val DEFAULT_API_KEY = "demo_api_key"
}
