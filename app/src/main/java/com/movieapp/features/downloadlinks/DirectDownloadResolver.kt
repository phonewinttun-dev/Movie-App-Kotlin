package com.movieapp.features.downloadlinks

import com.movieapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Lightweight resolver that extracts and validates direct downloadable video file URLs.
 * Bypasses countdown timers and prevents corrupt 78 kB HTML downloads.
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
     * Resolves a download link to an authenticated, verified direct video streaming URL.
     * Guarantees that only authentic video streams are returned, rejecting HTML portal pages.
     */
    suspend fun resolveDirectUrl(downloadLink: DownloadLinkDTO): Result<String> = withContext(Dispatchers.IO) {
        val rawUrl = downloadLink.url?.trim() ?: return@withContext Result.failure(Exception("Empty URL"))

        try {
            if (rawUrl.contains("megaup.net", ignoreCase = true)) {
                val directCandidate = extractMegaUpDirectLink(rawUrl)
                if (directCandidate.isSuccess) {
                    val candidateUrl = directCandidate.getOrThrow()
                    if (verifyDirectMediaUrl(candidateUrl)) {
                        Result.success(candidateUrl)
                    } else {
                        // Cloudflare challenge or auth required
                        Result.failure(Exception("Protected stream requires browser to complete full download"))
                    }
                } else {
                    directCandidate
                }
            } else if (isKnownWebPortal(rawUrl)) {
                // Portals like Yoteshin (Google Drive OAuth) or Usersdrive (captcha) cannot be downloaded directly
                // via background HTTP without user browser interaction
                Result.failure(Exception("Portal requires authentication to download full movie"))
            } else {
                // Verify if rawUrl is already a direct video stream
                if (verifyDirectMediaUrl(rawUrl)) {
                    Result.success(rawUrl)
                } else {
                    Result.failure(Exception("URL is not a direct video stream"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolves a download link specifically for copying to clipboard.
     * Bypasses countdown timers (e.g. MegaUp) so users can paste the direct link into external downloaders (1DM, ADM).
     */
    suspend fun resolveDirectUrlForCopy(downloadLink: DownloadLinkDTO): Result<String> = withContext(Dispatchers.IO) {
        val rawUrl = downloadLink.url?.trim() ?: return@withContext Result.failure(Exception("Empty URL"))

        try {
            if (rawUrl.contains("megaup.net", ignoreCase = true)) {
                val extracted = extractMegaUpDirectLink(rawUrl)
                if (extracted.isSuccess) {
                    extracted
                } else {
                    Result.success(rawUrl)
                }
            } else {
                Result.success(rawUrl)
            }
        } catch (e: Exception) {
            Result.success(rawUrl)
        }
    }

    /**
     * Checks whether a URL belongs to a known web portal that hosts HTML landing pages.
     */
    fun isKnownWebPortal(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("yoteshinportal.cc") ||
                lower.contains("usersdrive.com") ||
                lower.contains("bioscopeapp.com") ||
                lower.contains("drive.google.com/file")
    }

    /**
     * Extracts direct https://download.megaup.net/?url=... link from MegaUp HTML, bypassing the 5-second countdown timer.
     */
    fun extractMegaUpDirectLink(pageUrl: String): Result<String> {
        val request = Request.Builder()
            .url(pageUrl)
            .header("User-Agent", Constants.USER_AGENT)
            .build()

        return try {
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

                // Fallback: check alternate download link pattern
                val fallbackPattern = Pattern.compile("href=[\"'](https?://[^\"']+/(?:download|get)/[^\"']+)[\"']")
                val fallbackMatcher = fallbackPattern.matcher(html)
                if (fallbackMatcher.find()) {
                    val fallbackUrl = fallbackMatcher.group(1) ?: ""
                    if (fallbackUrl.isNotBlank()) return Result.success(fallbackUrl)
                }

                Result.failure(Exception("Direct download link not found in page"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Preflight check to verify that a target URL actually serves an authentic video stream
     * and is NOT an HTML error or portal page.
     */
    fun verifyDirectMediaUrl(url: String): Boolean {
        // First try HTTP HEAD request
        val headRequest = Request.Builder()
            .url(url)
            .header("User-Agent", Constants.USER_AGENT)
            .head()
            .build()

        try {
            client.newCall(headRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val contentType = response.header("Content-Type")?.lowercase() ?: ""
                    if (isMediaType(contentType)) {
                        return true
                    }
                    if (contentType.contains("text/html") || contentType.contains("text/plain")) {
                        return false
                    }
                }
            }
        } catch (_: Exception) {
            // If HEAD fails or is rejected (405), fallback to partial GET range check
        }

        // Fallback: Range GET request for first 1024 bytes
        val rangeRequest = Request.Builder()
            .url(url)
            .header("User-Agent", Constants.USER_AGENT)
            .header("Range", "bytes=0-1024")
            .build()

        return try {
            client.newCall(rangeRequest).execute().use { response ->
                if (response.isSuccessful || response.code == 206) {
                    val contentType = response.header("Content-Type")?.lowercase() ?: ""
                    isMediaType(contentType)
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Determines whether a Content-Type header corresponds to a valid video or binary stream.
     */
    fun isMediaType(contentType: String): Boolean {
        if (contentType.isBlank()) return false
        val lower = contentType.lowercase()
        if (lower.contains("text/html") || lower.contains("text/plain") || lower.contains("application/json")) {
            return false
        }
        return lower.startsWith("video/") ||
                lower.contains("application/octet-stream") ||
                lower.contains("application/x-matroska") ||
                lower.contains("binary/octet-stream") ||
                lower.contains("application/vnd.android.package-archive")
    }
}
