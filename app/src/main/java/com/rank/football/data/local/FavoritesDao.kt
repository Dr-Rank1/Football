package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(team: FavoriteTeam)

    @Query("DELETE FROM favorite_teams WHERE teamId = :teamId")
    suspend fun deleteFavorite(teamId: Int)

    @Query("SELECT * FROM favorite_teams ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteTeam>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_teams WHERE teamId = :teamId)")
    fun isFavorite(teamId: Int): Flow<Boolean>

    @Query("DELETE FROM favorite_teams")
    suspend fun clearAll()
}
