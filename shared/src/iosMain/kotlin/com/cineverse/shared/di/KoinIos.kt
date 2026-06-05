package com.cineverse.shared.di

import com.cineverse.core.analytics.AnalyticsTracker
import com.cineverse.core.analytics.DebugAnalyticsTracker
import com.cineverse.core.database.DatabaseDriverFactory
import com.cineverse.core.network.HttpClientEngineFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single { HttpClientEngineFactory() }

    single<AnalyticsTracker> { DebugAnalyticsTracker() }
}

fun initKoinSwift(analyticsTracker: AnalyticsTracker) = startKoin {
    modules(
        commonModule,
        module {
            single { DatabaseDriverFactory() }
            single { HttpClientEngineFactory() }
            single<AnalyticsTracker> { analyticsTracker }
        }
    )
}
