package com.wallstreet.domain.analytics

import android.os.Bundle

interface AnalyticsManager {
    fun logEvent(name: String, params: Map<String, Any?>? = null)
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun setUserId(userId: String)
    fun setUserProperty(name: String, value: String)
    fun logError(message: String, throwable: Throwable? = null)
}
