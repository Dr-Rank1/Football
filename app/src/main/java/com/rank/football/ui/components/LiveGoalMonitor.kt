package com.rank.football.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rank.football.GoalStreamApp
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.displayScore
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.data.repository.FootballRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

object LiveMatchBus {
    private val _matches = MutableStateFlow<List<FixtureItem>>(emptyList())
    val matches: StateFlow<List<FixtureItem>> = _matches.asStateFlow()

    fun publish(list: List<FixtureItem>) {
        _matches.value = list
    }
}

/** Polls live fixtures and emits [GoalAlertBus] events when scores increase. */
@Composable
fun LiveGoalMonitor(enabled: Boolean = true) {
    val context = LocalContext.current
    val app = context.applicationContext as GoalStreamApp
    val repository = remember { FootballRepository(context) }
    val favoritesRepository = remember { FavoritesRepository(AppDatabase.getInstance(context)) }
    val previousScores = remember { mutableMapOf<Int, Pair<Int, Int>>() }
    val goalAlertsEnabled by AppPreferences.goalAlerts(context).collectAsState(initial = true)

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        while (true) {
            try {
                app.syncStreamCatalog()
                val live = app.streamRepository.filterStreamable(repository.getLiveFixtures())
                LiveMatchBus.publish(live)
                if (goalAlertsEnabled) {
                    val favOnly = AppPreferences.favoriteOnlyNotifications(context).first()
                    val favIds = if (favOnly) {
                        favoritesRepository.allFavorites.first().map { it.teamId }.toSet()
                    } else {
                        emptySet()
                    }
                    detectGoals(live, previousScores, favOnly, favIds)
                } else {
                    // Still track scores so re-enabling doesn't flood toasts
                    live.forEach { f ->
                        previousScores[f.fixture.id] = (f.goals.home ?: 0) to (f.goals.away ?: 0)
                    }
                    previousScores.keys.retainAll(live.map { it.fixture.id }.toSet())
                }
            } catch (_: Exception) {
            }
            delay(45_000)
        }
    }
}

private fun detectGoals(
    live: List<FixtureItem>,
    previousScores: MutableMap<Int, Pair<Int, Int>>,
    favoritesOnly: Boolean,
    favoriteTeamIds: Set<Int>
) {
    live.forEach { fixture ->
        val home = fixture.goals.home ?: 0
        val away = fixture.goals.away ?: 0
        val id = fixture.fixture.id
        val prev = previousScores[id]
        if (prev != null) {
            val scoredHome = home > prev.first
            val scoredAway = away > prev.second
            if (scoredHome || scoredAway) {
                val involved = listOfNotNull(fixture.teams.home.id, fixture.teams.away.id)
                val allowed = !favoritesOnly || involved.any { it in favoriteTeamIds }
                if (allowed) {
                    val team = if (scoredHome) fixture.teams.home.name else fixture.teams.away.name
                    GoalAlertBus.emit(
                        GoalAlert(
                            fixtureId = id,
                            scorer = "Goal",
                            team = team,
                            minute = fixture.fixture.status.elapsed ?: 0,
                            score = fixture.displayScore(),
                            competition = fixture.league.name
                        )
                    )
                }
            }
        }
        previousScores[id] = home to away
    }
    val liveIds = live.map { it.fixture.id }.toSet()
    previousScores.keys.retainAll(liveIds)
}
