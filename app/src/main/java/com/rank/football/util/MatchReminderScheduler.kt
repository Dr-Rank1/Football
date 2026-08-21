package com.rank.football.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rank.football.workers.MatchReminderWorker
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object MatchReminderScheduler {

    fun scheduleReminder(
        context: Context,
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String,
        kickOffIso: String
    ) {
        val kickOff = try {
            LocalDateTime.parse(kickOffIso, DateTimeFormatter.ISO_DATE_TIME)
        } catch (_: Exception) {
            return
        }
        val triggerAt = kickOff.minusMinutes(15)
            .atZone(ZoneId.systemDefault())
            .toInstant()
        val delayMs = triggerAt.toEpochMilli() - System.currentTimeMillis()
        if (delayMs <= 0) return

        val data = Data.Builder()
            .putString(MatchReminderWorker.KEY_HOME, homeTeam)
            .putString(MatchReminderWorker.KEY_AWAY, awayTeam)
            .putInt(MatchReminderWorker.KEY_FIXTURE_ID, fixtureId)
            .build()

        val request = OneTimeWorkRequestBuilder<MatchReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$fixtureId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_$fixtureId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
