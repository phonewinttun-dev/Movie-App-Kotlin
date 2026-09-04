package com.movieapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movieapp.features.moviedetail.MovieDetailScreen
import com.movieapp.features.moviedetail.MovieDetailViewModel
import com.movieapp.features.movielist.MovieListScreen
import com.movieapp.features.movielist.MovieListViewModel
import com.movieapp.features.search.SearchScreen
import com.movieapp.features.search.SearchViewModel
import com.movieapp.navigation.Screen
import com.movieapp.theme.AppThemeController
import com.movieapp.theme.BlackTofuFontFamily
import com.movieapp.theme.CartoonFontFamily
import com.movieapp.theme.MovieAppTheme
import com.movieapp.theme.NeubrutalismIcons
import com.movieapp.theme.YoeshinFontFamily
import com.movieapp.theme.neoBorder
import com.movieapp.theme.neoColors
import com.movieapp.theme.neoShadow
import com.movieapp.util.AppLanguage
import com.movieapp.util.LocalizationManager
import com.movieapp.util.t

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        setContent {
            MovieAppTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val neoColors = MaterialTheme.neoColors

    val movieListViewModel: MovieListViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val movieDetailViewModel: MovieDetailViewModel = viewModel()

    Scaffold(
        containerColor = neoColors.background,
        topBar = {
            TopAppBarNeobrutalist(
                onSearchClick = {
                    if (currentRoute != Screen.Search.route) {
                        navController.navigate(Screen.Search.route)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationNeobrutalist(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Feed.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                MovieListScreen(
                    viewModel = movieListViewModel,
                    onTitleClick = { slug, isTv ->
                        movieDetailViewModel.loadDetail(slug, isTv)
                        navController.navigate(Screen.Detail.createRoute(slug, isTv))
                    }
                )
            }

            composable(Screen.Bookmarks.route) {
                com.movieapp.features.bookmarks.BookmarkScreen(
                    onTitleClick = { slug, isTv ->
                        movieDetailViewModel.loadDetail(slug, isTv)
                        navController.navigate(Screen.Detail.createRoute(slug, isTv))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onTitleClick = { slug, isTv ->
                        movieDetailViewModel.loadDetail(slug, isTv)
                        navController.navigate(Screen.Detail.createRoute(slug, isTv))
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("isTv") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                val isTv = backStackEntry.arguments?.getBoolean("isTv") ?: false

                androidx.compose.runtime.LaunchedEffect(slug, isTv) {
                    if (slug.isNotBlank()) {
                        movieDetailViewModel.loadDetail(slug, isTv)
                    }
                }

                MovieDetailScreen(
                    viewModel = movieDetailViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun TopAppBarNeobrutalist(
    onSearchClick: () -> Unit
) {
    val neoColors = MaterialTheme.neoColors
    val isDark = AppThemeController.isDarkMode
    val isNightLight = AppThemeController.isNightLightEnabled
    val currentLang = LocalizationManager.currentLanguage

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(neoColors.primary)
            .neoBorder(width = 2.5.dp, color = neoColors.border, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = t("app_title"),
                fontFamily = BlackTofuFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = neoColors.textPrimary
            )
            Text(
                text = t("app_subtitle"),
                fontFamily = YoeshinFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = neoColors.textPrimary
            )
        }

        // Quick Controls: Theme, Night Light, Language, Search (Vector Icons Only, No Emojis)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Mode Toggle (Light / Dark)
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 38.dp)
                    .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                    .background(neoColors.surface, RoundedCornerShape(8.dp))
                    .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                    .clickable { AppThemeController.toggleDarkMode() }
                    .semantics {
                        role = Role.Button
                        selected = isDark
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDark) NeubrutalismIcons.LightMode else NeubrutalismIcons.DarkMode,
                    contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = neoColors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Night Light Eye-Comfort Toggle (Dual-mode support for both Light & Dark)
            val nightLightBg = if (isNightLight) neoColors.secondary else neoColors.surface
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 38.dp)
                    .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                    .background(nightLightBg, RoundedCornerShape(8.dp))
                    .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                    .clickable { AppThemeController.toggleNightLight() }
                    .semantics {
                        role = Role.Button
                        selected = isNightLight
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = NeubrutalismIcons.NightLight,
                    contentDescription = if (isNightLight) t("night_light_on") else t("night_light_off"),
                    tint = neoColors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Language Toggle (EN / MY)
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 38.dp)
                    .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                    .background(neoColors.surface, RoundedCornerShape(8.dp))
                    .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                    .clickable { LocalizationManager.toggleLanguage() }
                    .semantics {
                        role = Role.Button
                        selected = currentLang == AppLanguage.MY
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = NeubrutalismIcons.Language,
                        contentDescription = "Toggle Language",
                        tint = neoColors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (currentLang == AppLanguage.EN) "EN" else "MY",
                        fontFamily = CartoonFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = neoColors.textPrimary
                    )
                }
            }

            // Search Icon Button
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 38.dp)
                    .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                    .background(neoColors.surface, RoundedCornerShape(8.dp))
                    .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                    .clickable(onClick = onSearchClick)
                    .semantics {
                        role = Role.Button
                        selected = false
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = NeubrutalismIcons.Search,
                    contentDescription = t("search_btn"),
                    tint = neoColors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavigationNeobrutalist(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val neoColors = MaterialTheme.neoColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(neoColors.surface)
            .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val items = listOf(
            Triple(Screen.Feed, t("nav_browse"), NeubrutalismIcons.Browse),
            Triple(Screen.Bookmarks, t("nav_bookmarks"), NeubrutalismIcons.Bookmark),
            Triple(Screen.Search, t("nav_search"), NeubrutalismIcons.Search)
        )

        items.forEach { (screen, label, icon) ->
            val isSelected = currentRoute == screen.route
            val bg = if (isSelected) neoColors.primary else neoColors.surfaceMuted
            val shadowOffset = if (isSelected) 3.dp else 0.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .neoShadow(offsetX = shadowOffset, offsetY = shadowOffset, color = neoColors.shadow, shape = RoundedCornerShape(10.dp))
                                .background(bg, RoundedCornerShape(10.dp))
                                .neoBorder(width = 2.5.dp, color = neoColors.border, shape = RoundedCornerShape(10.dp))
                        } else {
                            Modifier
                                .background(bg, RoundedCornerShape(10.dp))
                                .neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(10.dp))
                        }
                    )
                    .clickable { onNavigate(screen) }
                    .semantics {
                        role = Role.Tab
                        selected = isSelected
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = neoColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = label,
                        fontFamily = CartoonFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = neoColors.textPrimary
                    )
                }
            }
        }
    }
}
