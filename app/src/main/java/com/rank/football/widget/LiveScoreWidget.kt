package com.rank.football.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rank.football.workers.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

/** V5 home screen widget with compact, medium, and resizable large layouts. */
class LiveScoreWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scheduleUpdates(context)
        WidgetUpdateWorker.enqueue(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        WidgetUpdateWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_WORK_NAME)
    }

    companion object {
        const val WIDGET_WORK_NAME = "live_score_widget"

        /** Schedules periodic widget refresh every 2 minutes via WorkManager. */
        fun scheduleUpdates(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(2, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WIDGET_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
