package com.rank.football.data.local

import androidx.room.Entity

@Entity(tableName = "standings", primaryKeys = ["teamId", "leagueId"])
data class CachedStanding(
    val teamId: Int,
    val leagueId: Int,
    val position: Int,
    val teamName: String,
    val teamLogo: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int,
    val form: String,
    val lastUpdated: Long
)
