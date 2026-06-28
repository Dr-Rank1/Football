package com.rank.football.data.repository

import android.content.Context
import com.rank.football.data.api.RetrofitClient
import com.rank.football.data.model.FixtureEventItem
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.FixtureStatisticsItem
import com.rank.football.data.model.LeagueItem
import com.rank.football.data.model.LineupItem
import com.rank.football.data.model.PlayerSummary
import com.rank.football.data.model.StandingsLeagueItem
import com.rank.football.data.model.InjuryItem
import com.rank.football.data.model.TransferItem
import com.rank.football.data.model.TeamStatisticsItem
import com.rank.football.data.model.TeamSearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** FIFA World Cup league id in API-Football. */
const val WORLD_CUP_LEAGUE_ID = 1

class FootballRepository(context: Context) {

    private val appContext = context.applicationContext
    private val api get() = RetrofitClient.getApiService(appContext)

    suspend fun getLiveFixtures(): List<FixtureItem> = withContext(Dispatchers.IO) {
        runCatching { api.getLiveFixtures().response }.getOrDefault(emptyList())
    }

    suspend fun getFixturesByDate(date: LocalDate): List<FixtureItem> = withContext(Dispatchers.IO) {
        val formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        runCatching { api.getFixturesByDate(formatted).response }.getOrDefault(emptyList())
    }

    suspend fun getTodayFixtures(): List<FixtureItem> = getFixturesByDate(LocalDate.now())

    /** Returns World Cup fixtures for the current and previous season years. */
    suspend fun getWorldCupFixtures(): List<FixtureItem> = withContext(Dispatchers.IO) {
        val year = LocalDate.now().year
        (listOf(year, year - 1)).flatMap { season ->
            runCatching { api.getFixturesByLeague(WORLD_CUP_LEAGUE_ID, season).response }.getOrDefault(emptyList())
        }.distinctBy { it.fixture.id }
    }

    suspend fun getFixturesByLeague(leagueId: Int, season: Int): List<FixtureItem> =
        withContext(Dispatchers.IO) {
            runCatching { api.getFixturesByLeague(leagueId, season).response }.getOrDefault(emptyList())
        }

    suspend fun getStandings(leagueId: Int, season: Int): StandingsLeagueItem? =
        withContext(Dispatchers.IO) {
            runCatching { api.getStandings(leagueId, season).response.firstOrNull() }.getOrNull()
        }

    suspend fun getLeagues(): List<LeagueItem> = withContext(Dispatchers.IO) {
        runCatching { api.getLeagues().response }.getOrDefault(emptyList())
    }

    suspend fun searchLeagues(query: String): List<LeagueItem> = withContext(Dispatchers.IO) {
        runCatching { api.searchLeagues(query).response }.getOrDefault(emptyList())
    }

    suspend fun searchTeams(query: String): List<TeamSearchItem> = withContext(Dispatchers.IO) {
        runCatching { api.searchTeams(query).response }.getOrDefault(emptyList())
    }

    suspend fun getFixtureEvents(fixtureId: Int): List<FixtureEventItem> = withContext(Dispatchers.IO) {
        runCatching { api.getFixtureEvents(fixtureId).response }.getOrDefault(emptyList())
    }

    suspend fun getFixtureStatistics(fixtureId: Int): List<FixtureStatisticsItem> =
        withContext(Dispatchers.IO) {
            runCatching { api.getFixtureStatistics(fixtureId).response }.getOrDefault(emptyList())
        }

    suspend fun getFixtureLineups(fixtureId: Int): List<LineupItem> = withContext(Dispatchers.IO) {
        runCatching { api.getFixtureLineups(fixtureId).response }.getOrDefault(emptyList())
    }

    suspend fun getFixtureById(fixtureId: Int): FixtureItem? = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val dates = (-1..1).map { today.plusDays(it.toLong()) }
        for (date in dates) {
            val match = getFixturesByDate(date).find { it.fixture.id == fixtureId }
            if (match != null) return@withContext match
        }
        getLiveFixtures().find { it.fixture.id == fixtureId }
    }

    suspend fun searchFixtures(query: String): List<FixtureItem> = withContext(Dispatchers.IO) {
        val q = query.lowercase()
        val dates = (-3..3).map { LocalDate.now().plusDays(it.toLong()) }
        dates.flatMap { getFixturesByDate(it) }
            .filter {
                it.teams.home.name.lowercase().contains(q) ||
                    it.teams.away.name.lowercase().contains(q)
            }
            .distinctBy { it.fixture.id }
    }

    suspend fun getFixturesForTeamIds(teamIds: Set<Int>): List<FixtureItem> =
        withContext(Dispatchers.IO) {
            if (teamIds.isEmpty()) return@withContext emptyList()
            getTodayFixtures().filter {
                (it.teams.home.id in teamIds) || (it.teams.away.id in teamIds)
            }
        }

    suspend fun getPlayer(playerId: Int, season: Int): PlayerSummary? = withContext(Dispatchers.IO) {
        runCatching {
            val item = api.getPlayer(playerId, season).response.firstOrNull() ?: return@withContext null
            val stats = item.statistics?.firstOrNull()
            val goals = stats?.goals?.total ?: 0
            val assists = stats?.goals?.assists ?: 0
            val apps = stats?.games?.appearences ?: 0
            PlayerSummary(
                name = item.player.name,
                statsSummary = "Goals: $goals | Assists: $assists | Apps: $apps"
            )
        }.getOrNull()
    }

    suspend fun getHeadToHead(homeId: Int, awayId: Int): List<FixtureItem> = withContext(Dispatchers.IO) {
        runCatching { api.getHeadToHead("$homeId-$awayId").response }.getOrDefault(emptyList())
    }

    suspend fun getTeamForm(teamId: Int, leagueId: Int, season: Int): String = withContext(Dispatchers.IO) {
        runCatching { api.getTeamStats(teamId, season, leagueId).response.firstOrNull()?.form ?: "N/A" }
            .getOrDefault("N/A")
    }

    suspend fun getInjuries(fixtureId: Int): List<InjuryItem> = withContext(Dispatchers.IO) {
        runCatching { api.getInjuries(fixtureId).response }.getOrDefault(emptyList())
    }

    suspend fun getTransfers(teamId: Int, season: Int): List<TransferItem> = withContext(Dispatchers.IO) {
        runCatching { api.getTransfers(teamId, season).response }.getOrDefault(emptyList())
    }

    suspend fun getTeamStatistics(teamId: Int, leagueId: Int, season: Int): TeamStatisticsItem? =
        withContext(Dispatchers.IO) {
            runCatching {
                api.getTeamStatistics(teamId, season, leagueId).response.firstOrNull()
            }.getOrNull()
        }
}
