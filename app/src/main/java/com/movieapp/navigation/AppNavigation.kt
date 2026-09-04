package com.movieapp.navigation

/**
 * Screen destinations supported in the application.
 */
sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object Detail : Screen("detail/{slug}?isTv={isTv}") {
        fun createRoute(slug: String, isTvShow: Boolean): String {
            return "detail/$slug?isTv=$isTvShow"
        }
    }
}
