package com.rank.football.performance

import android.app.ActivityManager
import android.content.Context
import com.rank.football.analytics.LocalAnalytics

/** Tracks app performance metrics such as memory usage and slow operations. */
object AppPerformanceMonitor {

    /** Logs current process memory usage to local analytics. */
    fun logMemoryUsage(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val usedMb = (info.totalMem - info.availMem) / (1024 * 1024)
        LocalAnalytics.log(context, "memory_usage", mapOf("used_mb" to usedMb))
    }
}
