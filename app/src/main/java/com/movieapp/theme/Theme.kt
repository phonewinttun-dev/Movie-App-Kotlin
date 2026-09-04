package com.movieapp.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val NeoColorScheme = lightColorScheme(
    primary = NeoYellow,
    onPrimary = NeoBlack,
    secondary = NeoPink,
    onSecondary = NeoBlack,
    tertiary = NeoCyan,
    onTertiary = NeoBlack,
    background = NeoBackground,
    onBackground = NeoBlack,
    surface = NeoWhite,
    onSurface = NeoBlack
)

@Composable
fun MovieAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeoColorScheme,
        typography = Typography,
        content = content
    )
}
