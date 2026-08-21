package com.rank.football.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_teams")
data class FavoriteTeam(
    @PrimaryKey val teamId: Int,
    val teamName: String,
    val teamLogo: String,
    val leagueId: Int,
    val leagueName: String,
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "team")
    val favoriteType: String = "team"
)
