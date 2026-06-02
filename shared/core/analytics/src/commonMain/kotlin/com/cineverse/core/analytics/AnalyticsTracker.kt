package com.cineverse.core.analytics

interface AnalyticsTracker {
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())
}

class DebugAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(name: String, params: Map<String, Any>) {
        val paramsString = params.entries.joinToString { "${it.key}=${it.value}" }
        println("[Analytics] Event: '$name' with parameters: {$paramsString}")
    }
}
