package com.cineverse.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect class HttpClientEngineFactory {
    fun createEngine(): HttpClientEngine
}

fun createHttpClient(
    engineFactory: HttpClientEngineFactory,
    apiKey: String = TmdbConfig.API_KEY
): HttpClient {
    return HttpClient(engineFactory.createEngine()) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }
        
        defaultRequest {
            url("https://api.themoviedb.org/3/")
            if (apiKey.length > 50) {
                header("Authorization", "Bearer $apiKey")
            } else {
                url.parameters.append("api_key", apiKey)
            }
        }
    }
}
