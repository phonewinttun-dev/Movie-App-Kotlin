package com.movieapp.features.downloadlinks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Lightweight resolver that extracts direct downloadable file URLs by bypassing ad pages and timers.
 * Ponytail: zero added libraries, uses existing OkHttpClient & regex patterns.
 */
object DirectDownloadResolver {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    /**
     * Resolves a download link to a direct streaming/download URL.
     * Supports MegaUp timer bypass and direct link extraction.
     */
    suspend fun resolveDirectUrl(downloadLink: DownloadLinkDTO): Result<String> = withContext(Dispatchers.IO) {
        val rawUrl = downloadLink.url?.trim() ?: return@withContext Result.failure(Exception("Empty URL"))

        try {
            if (rawUrl.contains("megaup.net", ignoreCase = true)) {
                resolveMegaUp(rawUrl)
            } else if (rawUrl.contains("bioscopeapp.com", ignoreCase = true) || rawUrl.contains("yoteshinportal.cc", ignoreCase = true)) {
                // If it's direct or requires portal authentication
                Result.success(rawUrl)
            } else {
                // Fallback direct URL
                Result.success(rawUrl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts direct https://download.megaup.net/?url=... link from MegaUp HTML.
     */
    private fun resolveMegaUp(pageUrl: String): Result<String> {
        val request = Request.Builder()
            .url(pageUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP error ${response.code}"))
            }

            val html = response.body?.string() ?: return Result.failure(Exception("Empty response body"))

            // Match href='https://download.megaup.net/?url=...'
            val pattern = Pattern.compile("(https://download\\.megaup\\.net/\\?url=[^'\"\\s<>]+)")
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                val directUrl = matcher.group(1) ?: ""
                if (directUrl.isNotBlank()) return Result.success(directUrl)
            }

            // Fallback: check if there is an alternate download link button in the page
            val fallbackPattern = Pattern.compile("href=[\"'](https?://[^\"']+/(?:download|get)/[^\"']+)[\"']")
            val fallbackMatcher = fallbackPattern.matcher(html)
            if (fallbackMatcher.find()) {
                val fallbackUrl = fallbackMatcher.group(1) ?: ""
                if (fallbackUrl.isNotBlank()) return Result.success(fallbackUrl)
            }

            return Result.failure(Exception("Direct download link not found in page"))
        }
    }
}
