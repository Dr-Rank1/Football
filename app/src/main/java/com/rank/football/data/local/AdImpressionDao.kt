package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdImpressionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AdImpressionEntity)

    @Query("SELECT * FROM ad_impressions WHERE day = :day")
    fun byDay(day: String): Flow<List<AdImpressionEntity>>
}
