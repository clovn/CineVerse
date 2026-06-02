package com.cineverse.android

import android.app.Application
import com.cineverse.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class CineVerseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@CineVerseApp)
        }
    }
}
