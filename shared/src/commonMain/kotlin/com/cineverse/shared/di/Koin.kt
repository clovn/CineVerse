package com.cineverse.shared.di

import com.cineverse.core.database.createDatabase
import com.cineverse.core.network.createHttpClient
import com.cineverse.data.repository.MovieRepositoryImpl
import com.cineverse.data.repository.UserRepositoryImpl
import com.cineverse.domain.repository.MovieRepository
import com.cineverse.domain.repository.UserRepository
import com.cineverse.presentation.details.DetailsViewModel
import com.cineverse.presentation.dice.DiceViewModel
import com.cineverse.presentation.home.HomeViewModel
import com.cineverse.presentation.profile.ProfileViewModel
import com.cineverse.presentation.search.SearchViewModel
import com.cineverse.presentation.watchlist.WatchlistViewModel
import com.cineverse.presentation.main.MainViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule, platformModule)
}

fun initKoin() = initKoin {}

val commonModule = module {
    single { createDatabase(get()) }
    single { createHttpClient(get()) }
    
    single<MovieRepository> { MovieRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    
    factory { HomeViewModel(get(), get()) }
    factory { SearchViewModel(get(), get()) }
    factory { DetailsViewModel(get(), get(), get()) }
    factory { ProfileViewModel(get(), get()) }
    factory { WatchlistViewModel(get(), get()) }
    factory { DiceViewModel(get(), get()) }
    factory { MainViewModel(get()) }
}

expect val platformModule: Module
