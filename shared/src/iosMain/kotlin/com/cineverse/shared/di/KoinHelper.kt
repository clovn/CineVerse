package com.cineverse.shared.di

import com.cineverse.presentation.details.DetailsViewModel
import com.cineverse.presentation.dice.DiceViewModel
import com.cineverse.presentation.home.HomeViewModel
import com.cineverse.presentation.profile.ProfileViewModel
import com.cineverse.presentation.search.SearchViewModel
import com.cineverse.presentation.watchlist.WatchlistViewModel
import com.cineverse.presentation.main.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * iOS-friendly Koin accessor that provides properly typed ViewModel getters.
 * Swift can call these without dealing with KotlinKClass or Koin generics.
 */
class KoinHelper : KoinComponent {
    fun getHomeViewModel(): HomeViewModel = get()
    fun getSearchViewModel(): SearchViewModel = get()
    fun getDetailsViewModel(): DetailsViewModel = get()
    fun getProfileViewModel(): ProfileViewModel = get()
    fun getWatchlistViewModel(): WatchlistViewModel = get()
    fun getDiceViewModel(): DiceViewModel = get()
    fun getMainViewModel(): MainViewModel = get()
}
