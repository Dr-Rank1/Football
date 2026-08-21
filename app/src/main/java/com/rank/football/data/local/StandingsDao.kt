package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinLedgerDao {
    @Insert
    suspend fun insert(transaction: CoinTransaction)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_ledger")
    suspend fun balance(): Int

    @Query("SELECT * FROM coin_ledger ORDER BY timestamp DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<CoinTransaction>>
}

@Dao
interface StandingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<CachedStanding>)

    @Query("DELETE FROM standings WHERE leagueId = :leagueId")
    suspend fun deleteByLeague(leagueId: Int)

    @Query("SELECT * FROM standings WHERE leagueId = :leagueId ORDER BY position ASC")
    fun byLeague(leagueId: Int): Flow<List<CachedStanding>>
}
