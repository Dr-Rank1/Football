package com.rank.football.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rank.football.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReminderHelper {

    private const val CHANNEL_ID = "match_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(
        context: Context,
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String,
        kickOffIso: String
    ) {
        createNotificationChannel(context)
        val kickOff = try {
            LocalDateTime.parse(kickOffIso, DateTimeFormatter.ISO_DATE_TIME)
        } catch (_: Exception) {
            return
        }
        val triggerAt = kickOff.minusMinutes(15)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) return

        val intent = Intent(context, MatchReminderReceiver::class.java).apply {
            putExtra("home", homeTeam)
            putExtra("away", awayTeam)
            putExtra("fixtureId", fixtureId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            fixtureId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }
}

class MatchReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val home = intent.getStringExtra("home") ?: return
        val away = intent.getStringExtra("away") ?: return
        val fixtureId = intent.getIntExtra("fixtureId", 0)

        ReminderHelper.createNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, "match_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body, home, away))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(fixtureId, notification)
    }
}
