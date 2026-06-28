package com.rank.football.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rank.football.MainActivity
import com.rank.football.R

/** Creates notification channels and shows typed match notifications. */
object NotificationHelper {

    const val CHANNEL_GOALS = "goals"
    const val CHANNEL_REMINDERS = "match_reminders"
    const val CHANNEL_RESULTS = "results"
    const val CHANNEL_PLAYBACK = "playback"

    /** Registers all GoalStream notification channels. */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(CHANNEL_GOALS, context.getString(R.string.channel_goals), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_RESULTS, context.getString(R.string.channel_results), NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_PLAYBACK, context.getString(R.string.channel_playback), NotificationManager.IMPORTANCE_LOW)
        ).forEach { manager.createNotificationChannel(it) }
    }

    /** Shows a goal alert notification with deep link to the match. */
    fun showGoalNotification(context: Context, fixtureId: Int, body: String) {
        showNotification(context, CHANNEL_GOALS, fixtureId, context.getString(R.string.notification_goal_title), body, fixtureId)
    }

    /** Shows a match-starting notification. */
    fun showMatchStartNotification(context: Context, fixtureId: Int, body: String) {
        showNotification(context, CHANNEL_REMINDERS, fixtureId + 10000, context.getString(R.string.notification_start_title), body, fixtureId)
    }

    /** Shows a full-time result notification. */
    fun showResultNotification(context: Context, fixtureId: Int, body: String) {
        showNotification(context, CHANNEL_RESULTS, fixtureId + 20000, context.getString(R.string.notification_result_title), body, fixtureId)
    }

    private fun showNotification(
        context: Context,
        channel: String,
        notificationId: Int,
        title: String,
        body: String,
        fixtureId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fixtureId", fixtureId)
        }
        val pending = PendingIntent.getActivity(
            context, fixtureId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }
}
