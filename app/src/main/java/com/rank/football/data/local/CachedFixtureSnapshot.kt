package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Cached JSON snapshot of fixtures for offline fallback (live / date keys). */
@Entity(tableName = "fixture_snapshots")
data class CachedFixtureSnapshot(
    @PrimaryKey val cacheKey: String,
    val payloadJson: String,
    val cachedAt: Long
)

@Dao
interface FixtureSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: CachedFixtureSnapshot)

    @Query("SELECT * FROM fixture_snapshots WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): CachedFixtureSnapshot?
}
