package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntry(
    @PrimaryKey val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val leagueId: Int,
    val leagueName: String,
    val watchedAt: Long,
    val watchDurationSeconds: Int = 0
)
