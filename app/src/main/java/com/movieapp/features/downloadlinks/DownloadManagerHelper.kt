package com.movieapp.features.downloadlinks

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.movieapp.data.local.AppDatabase
import com.movieapp.data.local.DownloadEntity
import com.movieapp.util.LocalizationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            Toast.makeText(context, LocalizationManager.getString("copied"), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not copy link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches Telegram app strictly using tg:// deep link without falling back to web browser.
     */
    fun openTelegram(context: Context, url: String) {
        val tgDeepLink = convertToTelegramDeepLink(url) ?: run {
            if (url.startsWith("tg://")) url else null
        }

        if (tgDeepLink != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tgDeepLink)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    LocalizationManager.getString("install_telegram_prompt"),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        Toast.makeText(
            context,
            LocalizationManager.getString("install_telegram_prompt"),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Enqueues a verified direct file URL to Android's native DownloadManager and persists to Room.
     */
    fun startNativeDownload(
        context: Context,
        title: String,
        directUrl: String,
        movieSlug: String = "",
        poster: String? = null
    ): Long {
        if (DirectDownloadResolver.isKnownWebPortal(directUrl)) {
            Toast.makeText(context, "Cannot download portal page directly", Toast.LENGTH_SHORT).show()
            return -1L
        }

        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(directUrl)

            // Extract or sanitize filename
            val rawName = directUrl.substringAfterLast("/").substringBefore("?")
            val extension = if (rawName.contains(".")) ".${rawName.substringAfterLast(".")}" else ".mp4"
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            val fileName = "$sanitizedTitle$extension"
            val mimeType = if (extension.equals(".mkv", ignoreCase = true)) "video/x-matroska" else "video/mp4"

            val request = DownloadManager.Request(uri).apply {
                setTitle(title)
                setDescription("Downloading $title")
                setMimeType(mimeType)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadId = downloadManager.enqueue(request)

            // Persist record to Room database for active tracking and history
            CoroutineScope(Dispatchers.IO).launch {
                val entity = DownloadEntity(
                    downloadId = downloadId,
                    title = title,
                    movieSlug = movieSlug,
                    poster = poster,
                    fileName = fileName,
                    status = DownloadManager.STATUS_PENDING,
                    createdAt = System.currentTimeMillis()
                )
                AppDatabase.getInstance(context).downloadDao().insertOrUpdate(entity)
            }

            Toast.makeText(context, "${LocalizationManager.getString("download_started")}: $title", Toast.LENGTH_SHORT).show()
            downloadId
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed to start: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            -1L
        }
    }

    /**
     * Opens a completed download file using installed media players.
     */
    fun openDownloadedFile(context: Context, download: DownloadEntity) {
        try {
            val uri = download.fileUri?.let { Uri.parse(it) } ?: run {
                val file = java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    download.fileName
                )
                if (file.exists()) {
                    Uri.fromFile(file)
                } else null
            }

            if (uri == null) {
                Toast.makeText(context, "File not found on device", Toast.LENGTH_SHORT).show()
                return
            }

            val extension = download.fileName.substringAfterLast(".", "mp4")
            val mimeType = if (extension.equals("mkv", ignoreCase = true)) "video/x-matroska" else "video/mp4"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No video player found to play this file", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Deletes a downloaded video file from disk and deletes the task from Room.
     */
    fun deleteDownloadedFile(context: Context, download: DownloadEntity) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(download.downloadId)
        } catch (_: Exception) {}

        try {
            val file = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                download.fileName
            )
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context).downloadDao().deleteDownload(download.downloadId)
        }
        Toast.makeText(context, "Download removed", Toast.LENGTH_SHORT).show()
    }

    /**
     * General launcher for external links (web portal fallback).
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
