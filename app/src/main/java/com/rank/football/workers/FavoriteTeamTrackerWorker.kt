package com.rank.football.workers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.OnboardingPreference
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.data.repository.FootballRepository
import com.rank.football.data.model.isUpcoming
import com.rank.football.util.MatchReminderScheduler
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** Syncs next-match info for favorited teams every 6 hours. */
class FavoriteTeamTrackerWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val favorites = FavoritesRepository(db).allFavorites.first()
            val repo = FootballRepository(applicationContext)
            val store = OnboardingPreference.dataStore(applicationContext)
            favorites.forEach { fav ->
                val fixtures = repo.getFixturesByLeague(fav.leagueId, java.time.LocalDate.now().year)
                val next = fixtures.filter {
                    it.teams.home.id == fav.teamId || it.teams.away.id == fav.teamId
                }.firstOrNull { it.isUpcoming() }
                next?.let { match ->
                    store.edit { prefs ->
                        prefs[stringPreferencesKey("next_match_${fav.teamId}")] =
                            "${match.fixture.id}|${match.teams.home.name}|${match.teams.away.name}|${match.fixture.date}"
                    }
                    MatchReminderScheduler.scheduleReminder(
                        applicationContext,
                        match.fixture.id,
                        match.teams.home.name,
                        match.teams.away.name,
                        match.fixture.date
                    )
                }
            }
            store.edit { it[LAST_SYNC_KEY] = System.currentTimeMillis() }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private val LAST_SYNC_KEY = longPreferencesKey("favorite_tracker_last_sync")
        const val WORK_NAME = "favorite_team_tracker"

        /** Schedules periodic favorite team fixture tracking every 6 hours. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<FavoriteTeamTrackerWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
