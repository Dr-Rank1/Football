package com.rank.football.analytics

import android.content.Context
import com.rank.football.data.local.AnalyticsEvent
import com.rank.football.data.local.AppDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Logs analytics events locally to Room without third-party SDKs. */
object LocalAnalytics {
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Persists a named event with optional JSON-serialized params. */
    fun log(context: Context, eventName: String, params: Map<String, Any> = emptyMap()) {
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.analyticsDao().insert(
                    AnalyticsEvent(eventName = eventName, params = gson.toJson(params))
                )
                val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                db.analyticsDao().purgeBefore(cutoff)
            } catch (_: Exception) {
            }
        }
    }

    /** Returns the most recent events for debug display. */
    suspend fun recentEvents(context: Context, limit: Int = 50): List<AnalyticsEvent> {
        return AppDatabase.getInstance(context).analyticsDao().recent(limit)
    }
}
