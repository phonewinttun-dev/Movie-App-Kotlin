package com.movieapp.features.downloadlinks

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast

/**
 * Utility helper facilitating intent execution, Telegram deep linking, and Android DownloadManager enqueueing.
 */
object DownloadManagerHelper {

    /**
     * Converts a standard Telegram web URL (e.g. https://t.me/channel_name/123)
     * into a Telegram deep link URL (tg://resolve?domain=channel_name&post=123)
     * so it opens the Telegram app instantly without browser redirects.
     */
    fun convertToTelegramDeepLink(url: String): String? {
        val trimmed = url.trim()
        val regex = Regex("""(?:https?://)?(?:www\.)?(?:t\.me|telegram\.me)/([^/?#]+)(?:/(\d+))?""")
        val match = regex.find(trimmed) ?: return null
        val domain = match.groupValues.getOrNull(1) ?: return null
        val post = match.groupValues.getOrNull(2)

        return if (!post.isNullOrBlank()) {
            "tg://resolve?domain=$domain&post=$post"
        } else {
            "tg://resolve?domain=$domain"
        }
    }

    /**
     * Copies the given download link to the user's clipboard and displays a Toast confirmation.
     */
    fun copyLinkToClipboard(context: Context, label: String, url: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not copy link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches Telegram app using tg:// deep link, falling back to browser if Telegram is not installed.
     */
    fun openTelegram(context: Context, url: String) {
        val tgDeepLink = convertToTelegramDeepLink(url)
        if (tgDeepLink != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tgDeepLink)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Telegram client not installed, fall back to browser URL
            }
        }

        // Web fallback
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            copyLinkToClipboard(context, "Telegram", url)
        }
    }

    /**
     * Enqueues a direct file URL to Android's native DownloadManager.
     */
    fun startNativeDownload(context: Context, title: String, directUrl: String) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(directUrl)

            // Extract or sanitize filename
            val rawName = directUrl.substringAfterLast("/").substringBefore("?")
            val extension = if (rawName.contains(".")) ".${rawName.substringAfterLast(".")}" else ".mp4"
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            val fileName = "$sanitizedTitle$extension"

            val request = DownloadManager.Request(uri).apply {
                setTitle(title)
                setDescription("Downloading $title")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            downloadManager.enqueue(request)
            Toast.makeText(context, "Download started: $title", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed to start: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * General launcher for external links.
     */
    fun openExternalLink(context: Context, downloadLink: DownloadLinkDTO) {
        val rawUrl = downloadLink.url?.trim()
        if (rawUrl.isNullOrBlank()) {
            Toast.makeText(context, "Download link unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        if (downloadLink.isTelegram) {
            openTelegram(context, rawUrl)
            return
        }

        try {
            val parsedUri = Uri.parse(rawUrl)
            val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            copyLinkToClipboard(context, downloadLink.cleanServerName, rawUrl)
            Toast.makeText(context, "No app available to open URL. Copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }
}

