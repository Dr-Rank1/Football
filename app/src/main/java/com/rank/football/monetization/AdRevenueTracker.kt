package com.rank.football.monetization

import android.content.Context
import com.rank.football.data.local.AdImpressionEntity
import com.rank.football.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tracks local ad impressions and estimates revenue by ad type. */
object AdRevenueTracker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    enum class AdType(val cpm: Double) {
        BANNER(0.30),
        INTERSTITIAL(2.50),
        REWARDED(8.00),
        NATIVE(1.50),
        APP_OPEN(3.00),
        REWARDED_INTERSTITIAL(5.00)
    }

    /** Logs an ad impression for the given type on today's date. */
    fun logImpression(context: Context, type: AdType) {
        scope.launch {
            val db = AppDatabase.getInstance(context)
            val day = dayFormat.format(Date())
            db.adImpressionDao().insert(
                AdImpressionEntity(adType = type.name, day = day, count = 1)
            )
        }
    }

    /** Returns estimated daily revenue grouped by ad type. */
    fun dailyTotals(context: Context): Flow<Map<AdType, Double>> {
        val day = dayFormat.format(Date())
        return AppDatabase.getInstance(context).adImpressionDao().byDay(day).map { rows ->
            rows.groupBy { it.adType }.mapValues { (typeName, items) ->
                val type = AdType.entries.find { it.name == typeName } ?: AdType.BANNER
                val impressions = items.sumOf { it.count }
                type.cpm * impressions / 1000.0
            }.mapKeys { (key, _) ->
                AdType.entries.find { it.name == key } ?: AdType.BANNER
            }
        }
    }
}
