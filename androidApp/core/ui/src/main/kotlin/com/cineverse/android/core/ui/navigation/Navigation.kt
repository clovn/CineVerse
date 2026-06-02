package com.cineverse.android.core.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Details : Screen("details/{movieId}") {
        fun createRoute(movieId: Int) = "details/$movieId"
    }
    data object Profile : Screen("profile")
    data object Watchlist : Screen("watchlist")
    data object Dice : Screen("dice")
}
