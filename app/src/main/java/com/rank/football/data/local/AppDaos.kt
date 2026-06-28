package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchHistoryEntry)

    @Update
    suspend fun update(entry: WatchHistoryEntry)

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAll(): Flow<List<WatchHistoryEntry>>

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun count(): Int

    @Query("UPDATE watch_history SET watchDurationSeconds = :seconds WHERE fixtureId = :fixtureId")
    suspend fun updateDuration(fixtureId: Int, seconds: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface LeagueAffinityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(affinity: LeagueAffinity)

    @Query("SELECT * FROM league_affinity ORDER BY totalWatchSeconds DESC")
    fun getAll(): Flow<List<LeagueAffinity>>

    @Query("SELECT * FROM league_affinity WHERE leagueId = :leagueId LIMIT 1")
    suspend fun getById(leagueId: Int): LeagueAffinity?
}

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: Prediction)

    @Update
    suspend fun update(prediction: Prediction)

    @Query("SELECT * FROM predictions WHERE fixtureId = :fixtureId LIMIT 1")
    suspend fun getByFixture(fixtureId: Int): Prediction?

    @Query("SELECT * FROM predictions ORDER BY pointsEarned DESC")
    fun getAll(): Flow<List<Prediction>>

    @Query("SELECT SUM(pointsEarned) FROM predictions")
    suspend fun totalPoints(): Int?

    @Query("DELETE FROM predictions")
    suspend fun clearAll()
}

@Dao
interface AnalyticsDao {
    @Insert
    suspend fun insert(event: AnalyticsEvent)

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<AnalyticsEvent>

    @Query("DELETE FROM analytics_events WHERE timestamp < :before")
    suspend fun purgeBefore(before: Long)
}
