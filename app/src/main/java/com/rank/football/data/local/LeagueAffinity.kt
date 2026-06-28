package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "league_affinity")
data class LeagueAffinity(
    @PrimaryKey val leagueId: Int,
    val leagueName: String,
    var openCount: Int = 0,
    var totalWatchSeconds: Int = 0,
    var lastOpenedAt: Long = 0L
)
