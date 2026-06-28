package com.rank.football.util

import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isUpcoming
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object MatchCountdown {

    fun countdownText(fixture: FixtureItem): String? {
        if (!fixture.isUpcoming()) return null
        return try {
            val kickOff = LocalDateTime.parse(fixture.fixture.date, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.systemDefault())
            val now = java.time.Instant.now().atZone(ZoneId.systemDefault())
            val duration = Duration.between(now, kickOff)
            if (duration.isNegative) return null
            val hours = duration.toHours()
            val minutes = duration.toMinutesPart()
            when {
                hours > 0 -> "Starts in ${hours}h ${minutes}m"
                minutes > 0 -> "Starts in ${minutes}m"
                else -> "Starting soon"
            }
        } catch (_: Exception) {
            null
        }
    }
}
