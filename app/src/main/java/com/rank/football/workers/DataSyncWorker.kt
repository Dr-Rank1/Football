package com.rank.football.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rank.football.GoalStreamApp
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.CachedStanding
import com.rank.football.data.local.toCachedStanding
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.data.repository.FootballRepository
import com.rank.football.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** Periodically syncs fixtures and standings on WiFi only. */
class DataSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? GoalStreamApp ?: return Result.failure()
            app.syncStreamCatalog()
            val streamRepository = app.streamRepository
            val repo = FootballRepository(applicationContext)
            val db = AppDatabase.getInstance(applicationContext)
            val favorites = FavoritesRepository(db).allFavorites.first()
            val live = streamRepository.filterStreamable(repo.getLiveFixtures())
            val favoriteIds = favorites.map { it.teamId }.toSet()
            live.filter {
                it.teams.home.id in favoriteIds || it.teams.away.id in favoriteIds
            }.forEach { match ->
                NotificationHelper.showMatchStartNotification(
                    applicationContext,
                    match.fixture.id,
                    "${match.teams.home.name} vs ${match.teams.away.name} — Watch free"
                )
            }
            (0..3).forEach { offset ->
                repo.getFixturesByDate(LocalDate.now().plusDays(offset.toLong()))
            }
            favorites.map { it.leagueId }.distinct().take(3).forEach { leagueId ->
                syncStandings(repo, db, leagueId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /** Caches league standings rows into Room. */
    private suspend fun syncStandings(
        repo: FootballRepository,
        db: AppDatabase,
        leagueId: Int
    ) {
        val standings = repo.getStandings(leagueId, LocalDate.now().year) ?: return
        val table = standings.league.standings.firstOrNull().orEmpty()
        val now = System.currentTimeMillis()
        val rows = table.mapNotNull { it.toCachedStanding(leagueId, now) }
        db.standingsDao().deleteByLeague(leagueId)
        if (rows.isNotEmpty()) db.standingsDao().insertAll(rows)
    }

    companion object {
        const val WORK_NAME = "data_sync_worker"

        /** Schedules periodic WiFi-only data sync every 30 minutes. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DataSyncWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
