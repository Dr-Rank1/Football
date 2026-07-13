package com.rank.football.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteTeam::class,
        WatchHistoryEntry::class,
        LeagueAffinity::class,
        Prediction::class,
        AnalyticsEvent::class,
        CoinTransaction::class,
        CachedStanding::class,
        AdImpressionEntity::class,
        FantasyTeam::class,
        FantasyPlayer::class,
        FantasyGameweek::class,
        FantasyRosterEntry::class,
        GoalClipEntry::class,
        MatchStory::class,
        ArticleCache::class,
        CachedFixtureSnapshot::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun leagueAffinityDao(): LeagueAffinityDao
    abstract fun predictionDao(): PredictionDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun coinLedgerDao(): CoinLedgerDao
    abstract fun standingsDao(): StandingsDao
    abstract fun adImpressionDao(): AdImpressionDao
    abstract fun fantasyDao(): FantasyDao
    abstract fun goalClipDao(): GoalClipDao
    abstract fun matchStoryDao(): MatchStoryDao
    abstract fun articleCacheDao(): ArticleCacheDao
    abstract fun fixtureSnapshotDao(): FixtureSnapshotDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS watch_history (
                        fixtureId INTEGER PRIMARY KEY NOT NULL,
                        homeTeam TEXT NOT NULL,
                        awayTeam TEXT NOT NULL,
                        leagueId INTEGER NOT NULL,
                        leagueName TEXT NOT NULL,
                        watchedAt INTEGER NOT NULL,
                        watchDurationSeconds INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS league_affinity (
                        leagueId INTEGER PRIMARY KEY NOT NULL,
                        leagueName TEXT NOT NULL,
                        openCount INTEGER NOT NULL DEFAULT 0,
                        totalWatchSeconds INTEGER NOT NULL DEFAULT 0,
                        lastOpenedAt INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS predictions (
                        fixtureId INTEGER PRIMARY KEY NOT NULL,
                        predictedWinner TEXT NOT NULL,
                        predictedScore TEXT NOT NULL,
                        pointsEarned INTEGER NOT NULL DEFAULT 0,
                        resolved INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS analytics_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventName TEXT NOT NULL,
                        params TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS coin_ledger (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS standings (
                        teamId INTEGER PRIMARY KEY NOT NULL,
                        leagueId INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        teamName TEXT NOT NULL,
                        teamLogo TEXT NOT NULL,
                        played INTEGER NOT NULL,
                        won INTEGER NOT NULL,
                        drawn INTEGER NOT NULL,
                        lost INTEGER NOT NULL,
                        goalsFor INTEGER NOT NULL,
                        goalsAgainst INTEGER NOT NULL,
                        points INTEGER NOT NULL,
                        form TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS ad_impressions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        adType TEXT NOT NULL,
                        day TEXT NOT NULL,
                        count INTEGER NOT NULL
                    )"""
                )
                db.execSQL("ALTER TABLE favorite_teams ADD COLUMN favoriteType TEXT NOT NULL DEFAULT 'team'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS fantasy_team (
                        id TEXT PRIMARY KEY NOT NULL,
                        teamName TEXT NOT NULL,
                        formation TEXT NOT NULL,
                        totalPoints INTEGER NOT NULL DEFAULT 0,
                        weeklyPoints INTEGER NOT NULL DEFAULT 0,
                        budget REAL NOT NULL DEFAULT 100.0,
                        createdAt INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS fantasy_player (
                        playerId INTEGER PRIMARY KEY NOT NULL,
                        playerName TEXT NOT NULL,
                        teamName TEXT NOT NULL,
                        teamLogo TEXT NOT NULL,
                        position TEXT NOT NULL,
                        price REAL NOT NULL,
                        weeklyPoints INTEGER NOT NULL DEFAULT 0,
                        totalPoints INTEGER NOT NULL DEFAULT 0,
                        form REAL NOT NULL DEFAULT 0.0,
                        selectedByPercent REAL NOT NULL DEFAULT 0.0,
                        goals INTEGER NOT NULL DEFAULT 0,
                        assists INTEGER NOT NULL DEFAULT 0,
                        cleanSheets INTEGER NOT NULL DEFAULT 0,
                        yellowCards INTEGER NOT NULL DEFAULT 0,
                        isCaptain INTEGER NOT NULL DEFAULT 0,
                        isViceCaptain INTEGER NOT NULL DEFAULT 0,
                        fantasyTeamId TEXT NOT NULL DEFAULT ''
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fantasy_player_position ON fantasy_player(position)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fantasy_player_totalPoints ON fantasy_player(totalPoints)")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS fantasy_gameweek (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weekNumber INTEGER NOT NULL,
                        points INTEGER NOT NULL,
                        transfersMade INTEGER NOT NULL,
                        chipUsed TEXT NOT NULL DEFAULT ''
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS fantasy_roster (
                        fantasyTeamId TEXT NOT NULL,
                        playerId INTEGER NOT NULL,
                        slotPosition TEXT NOT NULL,
                        isBench INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(fantasyTeamId, playerId)
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS goal_clip_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fixtureId INTEGER NOT NULL,
                        goalMinute INTEGER NOT NULL,
                        scorerName TEXT NOT NULL,
                        detectedAt INTEGER NOT NULL,
                        clipStart INTEGER NOT NULL DEFAULT 0,
                        clipEnd INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS match_stories (
                        fixtureId INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        generatedAt INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS article_cache (
                        cacheKey TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        category TEXT NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_watch_history_leagueId ON watch_history(leagueId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_watch_history_watchedAt ON watch_history(watchedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_analytics_events_timestamp ON analytics_events(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_analytics_events_eventName ON analytics_events(eventName)")
            }
        }

        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS fixture_snapshots (
                        cacheKey TEXT PRIMARY KEY NOT NULL,
                        payloadJson TEXT NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )"""
                )
            }
        }
        /** Returns the singleton Room database with migrations through v5. */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goalstream.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
