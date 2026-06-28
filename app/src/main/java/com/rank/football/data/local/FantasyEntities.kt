package com.rank.football.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "fantasy_team")
data class FantasyTeam(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val teamName: String,
    val formation: String,
    val totalPoints: Int = 0,
    val weeklyPoints: Int = 0,
    val budget: Float = 100.0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fantasy_player",
    indices = [Index("position"), Index("totalPoints")]
)
data class FantasyPlayer(
    @PrimaryKey val playerId: Int,
    val playerName: String,
    val teamName: String,
    val teamLogo: String,
    val position: String,
    val price: Float,
    val weeklyPoints: Int = 0,
    val totalPoints: Int = 0,
    val form: Float = 0.0f,
    val selectedByPercent: Float = 0.0f,
    val goals: Int = 0,
    val assists: Int = 0,
    val cleanSheets: Int = 0,
    val yellowCards: Int = 0,
    val isCaptain: Boolean = false,
    val isViceCaptain: Boolean = false,
    val fantasyTeamId: String = ""
)

@Entity(tableName = "fantasy_gameweek")
data class FantasyGameweek(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weekNumber: Int,
    val points: Int,
    val transfersMade: Int,
    val chipUsed: String = ""
)

@Entity(tableName = "goal_clip_queue")
data class GoalClipEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fixtureId: Int,
    val goalMinute: Int,
    val scorerName: String,
    val detectedAt: Long,
    val clipStart: Long = 0,
    val clipEnd: Long = 0
)

@Entity(tableName = "match_stories")
data class MatchStory(
    @PrimaryKey val fixtureId: Int,
    val title: String,
    val body: String,
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "article_cache")
data class ArticleCache(
    @PrimaryKey val cacheKey: String,
    val title: String,
    val body: String,
    val category: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fantasy_roster",
    primaryKeys = ["fantasyTeamId", "playerId"]
)
data class FantasyRosterEntry(
    val fantasyTeamId: String,
    val playerId: Int,
    val slotPosition: String,
    val isBench: Boolean = false
)
