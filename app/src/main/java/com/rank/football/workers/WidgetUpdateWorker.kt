package com.rank.football.workers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rank.football.GoalStreamApp
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.repository.FootballRepository
import com.rank.football.widget.LiveScoreWidget
import com.rank.football.widget.WidgetRenderer

/** Fetches live fixtures and updates all GoalStream widget layouts. */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as GoalStreamApp
            app.syncStreamCatalog()
            val streamRepository = app.streamRepository
            val repo = FootballRepository(applicationContext)
            val live = streamRepository.filterStreamable(repo.getLiveFixtures())
            val upcoming = streamRepository.filterStreamable(repo.getTodayFixtures())
                .filter { it.isUpcoming() }
                .minByOrNull { it.fixture.date ?: "" }
            val manager = AppWidgetManager.getInstance(applicationContext)
            val component = ComponentName(applicationContext, LiveScoreWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { id ->
                val options: Bundle = manager.getAppWidgetOptions(id)
                val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
                val views = WidgetRenderer.render(
                    applicationContext, id, minW, minH, live, upcoming
                )
                manager.updateAppWidget(id, views)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        /** Enqueues a one-time widget update work request. */
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            )
        }
    }
}
