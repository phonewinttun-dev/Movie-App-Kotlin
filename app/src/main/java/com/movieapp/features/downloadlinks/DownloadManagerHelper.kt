package com.movieapp.features.downloadlinks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility helper facilitating intent execution and clipboard actions for download links.
 */
object DownloadManagerHelper {

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
     * Launches external download link in browser or Telegram app with fallback.
     */
    fun openExternalLink(context: Context, downloadLink: DownloadLinkDTO) {
        val rawUrl = downloadLink.url?.trim()
        if (rawUrl.isNullOrBlank()) {
            Toast.makeText(context, "Download link unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val parsedUri = Uri.parse(rawUrl)
            val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: copy link and alert user
            copyLinkToClipboard(context, downloadLink.cleanServerName, rawUrl)
            Toast.makeText(context, "No app available to open URL. Copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }
}
