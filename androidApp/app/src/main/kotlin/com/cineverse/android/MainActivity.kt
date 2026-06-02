package com.cineverse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cineverse.core.designsystem.theme.CineVerseTheme
import com.cineverse.android.core.ui.navigation.Screen
import com.cineverse.android.features.details.DetailsScreen
import com.cineverse.android.features.dice.DiceScreen
import com.cineverse.android.features.home.HomeScreen
import com.cineverse.android.features.profile.ProfileScreen
import com.cineverse.android.features.profile.OnboardingScreen
import com.cineverse.android.features.profile.AuthScreen
import com.cineverse.android.features.search.SearchScreen
import com.cineverse.android.features.watchlist.WatchlistScreen
import com.cineverse.presentation.main.MainViewModel
import com.cineverse.presentation.main.MainIntent
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CineVerseTheme {
                val mainViewModel: MainViewModel = koinInject()
                val mainState by mainViewModel.state.collectAsState()

                if (mainState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (!mainState.isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = { mainViewModel.sendIntent(MainIntent.CompleteOnboarding) }
                    )
                } else if (!mainState.isAuthorized) {
                    val snackbarHostState = remember { SnackbarHostState() }
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        AuthScreen(
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    val navController = rememberNavController()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val navBackStackEntry = navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry.value?.destination?.route

                    val bottomNavItems = listOf(
                        Triple(Screen.Home.route, "Home", Icons.Default.Home),
                        Triple(Screen.Search.route, "Search", Icons.Default.Search),
                        Triple(Screen.Dice.route, "Dice", Icons.Default.Casino),
                        Triple(Screen.Watchlist.route, "Watchlist", Icons.Default.List),
                        Triple(Screen.Profile.route, "Profile", Icons.Default.Person)
                    )

                    // Display bottom navigation bar only when on root tabs
                    val showBottomBar = bottomNavItems.any { it.first == currentRoute }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    bottomNavItems.forEach { (route, label, icon) ->
                                        NavigationBarItem(
                                            selected = currentRoute == route,
                                            onClick = {
                                                if (currentRoute != route) {
                                                    navController.navigate(route) {
                                                        popUpTo(Screen.Home.route) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = { Icon(imageVector = icon, contentDescription = label) },
                                            label = { Text(label) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    onNavigateToDetails = { id -> navController.navigate(Screen.Details.createRoute(id)) },
                                    snackbarHostState = snackbarHostState
                                )
                            }
                            composable(Screen.Search.route) {
                                SearchScreen(
                                    onNavigateToDetails = { id -> navController.navigate(Screen.Details.createRoute(id)) }
                                )
                            }
                            composable(Screen.Dice.route) {
                                DiceScreen(
                                    onNavigateToDetails = { id -> navController.navigate(Screen.Details.createRoute(id)) }
                                )
                            }
                            composable(Screen.Watchlist.route) {
                                WatchlistScreen(
                                    onNavigateToDetails = { id -> navController.navigate(Screen.Details.createRoute(id)) },
                                    snackbarHostState = snackbarHostState
                                )
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(
                                    snackbarHostState = snackbarHostState
                                )
                            }
                            composable(
                                route = Screen.Details.route,
                                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                                DetailsScreen(
                                    movieId = movieId,
                                    onNavigateBack = { navController.popBackStack() },
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
