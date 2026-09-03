package com.movieapp.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lightweight, accessible vector icons inspired by Heroicons (https://heroicons.com/).
 * Built without heavy third-party dependencies to keep APK size minimal (Ponytail principle).
 */
object Heroicons {
    val Search: ImageVector get() = Icons.Default.Search
    val Clear: ImageVector get() = Icons.Default.Clear
    val ArrowLeft: ImageVector get() = Icons.AutoMirrored.Filled.ArrowBack
    val Refresh: ImageVector get() = Icons.Default.Refresh
    val Star: ImageVector get() = Icons.Default.Star
}
