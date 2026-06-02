package com.cineverse.shared.di

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.core.analytics.DebugAnalyticsTracker
import com.cineverse.core.database.DatabaseDriverFactory
import com.cineverse.core.network.HttpClientEngineFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single { HttpClientEngineFactory() }
    
    // Platform binding of the Analytics tracker (Debug implementation as fallback)
    single<AnalyticsTracker> { DebugAnalyticsTracker() }
}
