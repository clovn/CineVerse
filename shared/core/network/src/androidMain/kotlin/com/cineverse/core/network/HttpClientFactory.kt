package com.cineverse.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual class HttpClientEngineFactory {
    actual fun createEngine(): HttpClientEngine {
        return OkHttp.create()
    }
}
