package com.movieapp.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Classic Neobrutalism Base Accents
val NeoYellow = Color(0xFFFFE600)
val NeoCyan = Color(0xFF00F0FF)
val NeoPink = Color(0xFFFF6B8B)
val NeoGreen = Color(0xFF00E599)
val NeoBlack = Color(0xFF000000)
val NeoWhite = Color(0xFFFFFFFF)

// Static Fallbacks for existing imports
val NeoBackground = Color(0xFFF4F0EA)
val NeoCardBg = Color(0xFFFFFFFF)
val NeoMuted = Color(0xFF4A4A4A)
val NeoBorder = Color(0xFF000000)
val NeoError = Color(0xFFD32F2F)
val NeoErrorBackground = Color(0xFFFFEBEE)

@Immutable
data class NeoColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val shadow: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val errorBackground: Color
)

// 1. Light Mode (Warm Eye-Comfort / Night-Light Supportive)
val LightNeoColors = NeoColors(
    background = Color(0xFFFBF3E4),
    surface = Color(0xFFFFFDF7),
    surfaceMuted = Color(0xFFF2E6D0),
    textPrimary = Color(0xFF1F1710),
    textSecondary = Color(0xFF6B5848),
    border = Color(0xFF2D2319),
    shadow = Color(0xFF3D2E1E),
    primary = Color(0xFFF5D020),
    secondary = NeoPink,
    tertiary = NeoCyan,
    error = Color(0xFFC62828),
    errorBackground = Color(0xFFFFEBEE)
)

// 2. Dark Mode (Warm Eye-Comfort / Night-Light Supportive)
val DarkNeoColors = NeoColors(
    background = Color(0xFF1A1612),
    surface = Color(0xFF26201A),
    surfaceMuted = Color(0xFF332A22),
    textPrimary = Color(0xFFFFF5E6),
    textSecondary = Color(0xFFC7B39E),
    border = Color(0xFFE6C280),
    shadow = Color(0xFF0F0C09),
    primary = Color(0xFFFFD043),
    secondary = Color(0xFFFF859A),
    tertiary = Color(0xFF5CE1E6),
    error = Color(0xFFFF6E6E),
    errorBackground = Color(0xFF2D1B1B)
)
