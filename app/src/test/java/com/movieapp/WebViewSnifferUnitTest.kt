package com.movieapp

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.movieapp.features.downloadlinks.DirectDownloadResolver
import com.movieapp.features.downloadlinks.DownloadLinkDTO
import com.movieapp.features.downloadlinks.DownloadManagerHelper
import com.movieapp.features.downloadlinks.WebViewDownloadSniffer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Trait tag annotation conforming to /test-tagging taxonomy.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Trait(val category: String, val secondary: String = "")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewSnifferUnitTest {

    @Test
    @Trait(category = "positive", secondary = "critical-path")
    fun testSnifferMediaDetection_validVideoMime_recognizedAsDownloadable() {
        // Direct media streams and MIME types
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://storage.com/file", "video/mp4"))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://cdn.example.com/stream", "application/x-matroska"))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://host.net/cdn", "binary/octet-stream"))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://host.net/video.mkv", null))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://host.net/video.mp4?token=123", null))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://host.net/video.webm", null))
    }

    @Test
    @Trait(category = "negative", secondary = "boundary")
    fun testSnifferMediaDetection_htmlAndPlainText_rejected() {
        // Web portal pages, HTML, JSON, and errors must NOT be intercepted as video files
        assertFalse(WebViewDownloadSniffer.isMediaStream("https://megaup.net/34b80bbb", "text/html"))
        assertFalse(WebViewDownloadSniffer.isMediaStream("https://megaup.net/page.html", null))
        assertFalse(WebViewDownloadSniffer.isMediaStream("https://usersdrive.com/sample.php", null))
        assertFalse(WebViewDownloadSniffer.isMediaStream("https://api.example.com/details", "application/json"))
        assertFalse(WebViewDownloadSniffer.isMediaStream("", null))
        assertFalse(WebViewDownloadSniffer.isMediaStream(null, null))
    }

    @Test
    @Trait(category = "positive", secondary = "boundary")
    fun testSnifferMediaDetection_downloadEndpoints_recognized() {
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://download.megaup.net/?url=abc", "application/octet-stream"))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://cdn.example.com/download/stream123", "video/mp4"))
        assertTrue(WebViewDownloadSniffer.isMediaStream("https://storage.googleapis.com/movie-bucket/sample", "video/mp4"))
    }

    @Test
    @Trait(category = "positive", secondary = "integration")
    fun testCookieAndUserAgentInjectionInDownloadManager() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directUrl = "https://cdn.example.com/movies/sample_video.mp4"
        val testCookies = "cf_clearance=abcd1234efgh; session=xyz987"
        val testUserAgent = WebViewDownloadSniffer.CHROME_USER_AGENT

        // Verify request builds with cookies and userAgent without throwing
        val uri = Uri.parse(directUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle("Test Movie")
            addRequestHeader("Cookie", testCookies)
            addRequestHeader("User-Agent", testUserAgent)
        }
        assertNotNull(request)

        // Enqueue via DownloadManagerHelper
        val downloadId = DownloadManagerHelper.startNativeDownload(
            context = context,
            title = "Test Movie",
            directUrl = directUrl,
            cookies = testCookies,
            userAgent = testUserAgent
        )
        assertTrue("DownloadManager must successfully enqueue request (id >= 0)", downloadId >= 0)
    }

    @Test
    @Trait(category = "positive", secondary = "smoke")
    fun testExternalDownloader_packagesConfigured() {
        val pkgs = DownloadManagerHelper.EXTERNAL_DOWNLOADER_PACKAGES
        assertTrue(pkgs.contains("idm.internet.download.manager"))
        assertTrue(pkgs.contains("idm.internet.download.manager.plus"))
        assertTrue(pkgs.contains("com.dv.adm"))
    }

    @Test
    @Trait(category = "negative", secondary = "resilience")
    fun testExternalDownloader_emptyUrl_returnsFalse() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertFalse(DownloadManagerHelper.openInExternalDownloader(context, ""))
        assertFalse(DownloadManagerHelper.openInExternalDownloader(context, "   "))
    }

    @Test
    @Trait(category = "positive", secondary = "boundary")
    fun testIsKnownWebPortal_excludesDirectMediaUrls() {
        // Direct media files even hosted on portal domains are valid media files
        assertFalse(DirectDownloadResolver.isKnownWebPortal("https://usersdrive.com/files/movie.mp4"))
        assertFalse(DirectDownloadResolver.isKnownWebPortal("https://yoteshinportal.cc/download/movie.mkv"))

        // HTML portal pages
        assertTrue(DirectDownloadResolver.isKnownWebPortal("https://usersdrive.com/sample999.html"))
        assertTrue(DirectDownloadResolver.isKnownWebPortal("https://yoteshinportal.cc/hydra-2025"))
        assertTrue(DirectDownloadResolver.isKnownWebPortal("https://drive.google.com/file/d/123/view"))
    }

    @Test
    @Trait(category = "negative", secondary = "boundary")
    fun testDirectDownloadResolver_emptyUrl_failsGracefully() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val emptyDto = DownloadLinkDTO(url = "")
        val result = DirectDownloadResolver.resolveDirectUrl(context, emptyDto)
        assertTrue(result.isFailure)
        assertEquals("Empty URL", result.exceptionOrNull()?.message)
    }

    private fun assertNotNull(obj: Any?) {
        assertTrue(obj != null)
    }
}