package com.movieapp

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
import com.movieapp.theme.MovieAppTheme
import com.movieapp.theme.NeoBackground
import com.movieapp.theme.NeoBlack
import com.movieapp.theme.NeoWhite
import com.movieapp.theme.NeoYellow
import com.movieapp.theme.neoBorder
import com.movieapp.theme.neoShadow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    val movieListViewModel: MovieListViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val movieDetailViewModel: MovieDetailViewModel = viewModel()

    Scaffold(
        containerColor = NeoBackground,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoYellow)
            .neoBorder(shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Movie Catalog",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = NeoBlack
            )
            Text(
                text = "Find what to watch",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBlack
            )
        }

        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .neoShadow(offsetX = 2.dp, offsetY = 2.dp, shape = RoundedCornerShape(8.dp))
                .background(NeoWhite, RoundedCornerShape(8.dp))
                .neoBorder(width = 2.dp, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = onSearchClick)
                .semantics {
                    role = Role.Button
                    selected = false
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = com.movieapp.theme.Heroicons.Search,
                    contentDescription = null,
                    tint = NeoBlack,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Search",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoWhite)
            .neoBorder(shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val items = listOf(
            Screen.Feed to "Browse",
            Screen.Search to "Search"
        )

        items.forEach { (screen, label) ->
            val isSelected = currentRoute == screen.route
            val bg = if (isSelected) NeoYellow else NeoWhite
            val shadowOffset = if (isSelected) 3.dp else 0.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .neoShadow(offsetX = shadowOffset, offsetY = shadowOffset, shape = RoundedCornerShape(10.dp))
                                .background(bg, RoundedCornerShape(10.dp))
                                .neoBorder(shape = RoundedCornerShape(10.dp))
                        } else {
                            Modifier
                                .background(bg, RoundedCornerShape(10.dp))
                                .neoBorder(width = 1.5.dp, shape = RoundedCornerShape(10.dp))
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
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
                )
            }
        }
    }
}

