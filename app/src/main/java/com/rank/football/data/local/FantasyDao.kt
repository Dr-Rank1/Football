package com.rank.football.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FantasyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeam(team: FantasyTeam)

    @Query("SELECT * FROM fantasy_team LIMIT 1")
    fun observeTeam(): Flow<FantasyTeam?>

    @Query("SELECT * FROM fantasy_team LIMIT 1")
    suspend fun getTeam(): FantasyTeam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayers(players: List<FantasyPlayer>)

    @Query("SELECT * FROM fantasy_player WHERE fantasyTeamId = :teamId OR fantasyTeamId = '' ORDER BY totalPoints DESC")
    fun observeSquad(teamId: String): Flow<List<FantasyPlayer>>

    @Query("SELECT * FROM fantasy_player ORDER BY totalPoints DESC LIMIT :limit")
    suspend fun topPlayers(limit: Int = 50): List<FantasyPlayer>

    @Query("SELECT * FROM fantasy_roster WHERE fantasyTeamId = :teamId ORDER BY slotPosition ASC")
    fun observeRoster(teamId: String): Flow<List<FantasyRosterEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoster(entries: List<FantasyRosterEntry>)

    @Query("SELECT * FROM fantasy_roster WHERE fantasyTeamId = :teamId")
    suspend fun rosterForTeam(teamId: String): List<FantasyRosterEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameweek(week: FantasyGameweek)

    @Query("SELECT * FROM fantasy_gameweek ORDER BY weekNumber DESC")
    fun observeGameweeks(): Flow<List<FantasyGameweek>>
}

@Dao
interface GoalClipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GoalClipEntry)

    @Query("SELECT * FROM goal_clip_queue ORDER BY detectedAt DESC")
    fun observeAll(): Flow<List<GoalClipEntry>>

    @Query("SELECT * FROM goal_clip_queue WHERE fixtureId = :fixtureId ORDER BY detectedAt DESC LIMIT 1")
    suspend fun latestForFixture(fixtureId: Int): GoalClipEntry?
}

@Dao
interface MatchStoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: MatchStory)

    @Query("SELECT * FROM match_stories WHERE fixtureId = :fixtureId LIMIT 1")
    suspend fun get(fixtureId: Int): MatchStory?
}

@Dao
interface ArticleCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: ArticleCache)

    @Query("SELECT * FROM article_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): ArticleCache?

    @Query("DELETE FROM article_cache WHERE cachedAt < :before")
    suspend fun purgeBefore(before: Long)
}
