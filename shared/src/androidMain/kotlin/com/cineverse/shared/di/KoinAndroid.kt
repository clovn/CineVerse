package com.cineverse.shared.di

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.core.analytics.AndroidFirebaseAnalyticsTracker
import com.cineverse.core.database.DatabaseDriverFactory
import com.cineverse.core.network.HttpClientEngineFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { HttpClientEngineFactory() }

    single<AnalyticsTracker> { AndroidFirebaseAnalyticsTracker(androidContext()) }
}
