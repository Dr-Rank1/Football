package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.repository.FootballRepository
import com.rank.football.data.repository.WORLD_CUP_LEAGUE_ID
import com.rank.football.data.repository.StreamRepository
import com.rank.football.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Tab filter for streamable fixtures inside a league detail view. */
enum class LeagueFixtureTab { LIVE, TODAY, UPCOMING }

/** League row enriched with live stream counts from real fixture data. */
data class StreamableLeague(
    val id: Int,
    val name: String,
    val logo: String?,
    val country: String?,
    val countryFlag: String?,
    val season: Int,
    val liveCount: Int,
    val todayCount: Int,
    val upcomingCount: Int
)

/** Compact standings row for the league detail preview table. */
data class LeagueStandingPreview(
    val rank: Int,
    val teamName: String,
    val teamLogo: String?,
    val points: Int,
    val played: Int
)

class LeaguesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GoalStreamApp
    private val repository = FootballRepository(application)
    private val streamRepository: StreamRepository = app.streamRepository

    private val _overviewLoading = MutableStateFlow(true)
    val overviewLoading: StateFlow<Boolean> = _overviewLoading.asStateFlow()

    private val _leagues = MutableStateFlow<Result<List<StreamableLeague>>>(Result.Loading)
    val leagues: StateFlow<Result<List<StreamableLeague>>> = _leagues.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredLeagues = MutableStateFlow<List<StreamableLeague>>(emptyList())
    val filteredLeagues: StateFlow<List<StreamableLeague>> = _filteredLeagues.asStateFlow()

    private val _liveStreamCount = MutableStateFlow(0)
    val liveStreamCount: StateFlow<Int> = _liveStreamCount.asStateFlow()

    private val _todayStreamCount = MutableStateFlow(0)
    val todayStreamCount: StateFlow<Int> = _todayStreamCount.asStateFlow()

    private val _catalogConfigured = MutableStateFlow(false)
    val catalogConfigured: StateFlow<Boolean> = _catalogConfigured.asStateFlow()

    private val _selectedLeague = MutableStateFlow<StreamableLeague?>(null)
    val selectedLeague: StateFlow<StreamableLeague?> = _selectedLeague.asStateFlow()

    private val _fixtureTab = MutableStateFlow(LeagueFixtureTab.LIVE)
    val fixtureTab: StateFlow<LeagueFixtureTab> = _fixtureTab.asStateFlow()

    private val _leagueFixtures = MutableStateFlow<Result<List<FixtureItem>>>(Result.Loading)
    val leagueFixtures: StateFlow<Result<List<FixtureItem>>> = _leagueFixtures.asStateFlow()

    private val _standingsPreview = MutableStateFlow<List<LeagueStandingPreview>>(emptyList())
    val standingsPreview: StateFlow<List<LeagueStandingPreview>> = _standingsPreview.asStateFlow()

    private var allStreamableByLeague: Map<Int, List<FixtureItem>> = emptyMap()

    init {
        loadOverview()
    }

    /** Refreshes leagues that currently have streamable fixtures. */
    fun loadOverview() {
        viewModelScope.launch {
            _overviewLoading.value = true
            _leagues.value = Result.Loading
            try {
                app.syncStreamCatalog()
                _catalogConfigured.value = streamRepository.catalogLoaded.value
                val today = LocalDate.now()
                val live = streamRepository.filterStreamable(repository.getLiveFixtures())
                val todayFixtures = streamRepository.filterStreamable(repository.getFixturesByDate(today))
                val upcomingWindow = (1..7).flatMap { offset ->
                    streamRepository.filterStreamable(
                        repository.getFixturesByDate(today.plusDays(offset.toLong()))
                    )
                }.distinctBy { it.fixture.id }

                val worldCup = streamRepository.filterStreamable(repository.getWorldCupFixtures())

                _liveStreamCount.value = live.size
                _todayStreamCount.value = todayFixtures.size

                val grouped = (live + todayFixtures + upcomingWindow + worldCup)
                    .distinctBy { it.fixture.id }
                    .groupBy { it.league.id }

                allStreamableByLeague = grouped
                val overview = grouped.map { (leagueId, fixtures) ->
                    val sample = fixtures.first()
                    val season = sample.league.season ?: today.year
                    StreamableLeague(
                        id = leagueId,
                        name = sample.league.name,
                        logo = sample.league.logo,
                        country = sample.league.country,
                        countryFlag = null,
                        season = season,
                        liveCount = fixtures.count { it.isLive() },
                        todayCount = fixtures.count { isOnDate(it, today) },
                        upcomingCount = fixtures.count { it.isUpcoming() && !isOnDate(it, today) }
                    )
                }.sortedWith(
                    compareByDescending<StreamableLeague> { it.id == WORLD_CUP_LEAGUE_ID }
                        .thenByDescending { it.liveCount }
                        .thenByDescending { it.todayCount }
                        .thenBy { it.name }
                )

                _leagues.value = Result.Success(overview)
                applySearchFilter(_searchQuery.value, overview)
            } catch (e: Exception) {
                _leagues.value = Result.Error(e.message ?: "Unknown error")
            } finally {
                _overviewLoading.value = false
            }
        }
    }

    /** Updates the league search query and filters the visible list. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val current = (_leagues.value as? Result.Success)?.data.orEmpty()
        applySearchFilter(query, current)
    }

    /** Opens league detail with standings preview and streamable fixtures. */
    fun selectLeague(league: StreamableLeague) {
        _selectedLeague.value = league
        _fixtureTab.value = when {
            league.liveCount > 0 -> LeagueFixtureTab.LIVE
            league.todayCount > 0 -> LeagueFixtureTab.TODAY
            else -> LeagueFixtureTab.UPCOMING
        }
        loadLeagueDetail(league)
    }

    /** Clears league detail and returns to the overview list. */
    fun clearSelection() {
        _selectedLeague.value = null
        _leagueFixtures.value = Result.Loading
        _standingsPreview.value = emptyList()
    }

    /** Switches the active fixture tab inside league detail. */
    fun setFixtureTab(tab: LeagueFixtureTab) {
        _fixtureTab.value = tab
        _selectedLeague.value?.let { refreshFixturesForTab(it, tab) }
    }

    private fun loadLeagueDetail(league: StreamableLeague) {
        viewModelScope.launch {
            _leagueFixtures.value = Result.Loading
            try {
                val standings = repository.getStandings(league.id, league.season)
                _standingsPreview.value = standings?.league?.standings?.firstOrNull()
                    .orEmpty()
                    .take(5)
                    .map { row ->
                        LeagueStandingPreview(
                            rank = row.rank,
                            teamName = row.team.name,
                            teamLogo = row.team.logo,
                            points = row.points,
                            played = row.all.played
                        )
                    }

                val cached = allStreamableByLeague[league.id]
                if (!cached.isNullOrEmpty()) {
                    refreshFixturesForTab(league, _fixtureTab.value, cached)
                } else {
                    val seasonFixtures = streamRepository.filterStreamable(
                        repository.getFixturesByLeague(league.id, league.season)
                    )
                    allStreamableByLeague = allStreamableByLeague + (league.id to seasonFixtures)
                    refreshFixturesForTab(league, _fixtureTab.value, seasonFixtures)
                }
            } catch (e: Exception) {
                _leagueFixtures.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun refreshFixturesForTab(
        league: StreamableLeague,
        tab: LeagueFixtureTab,
        source: List<FixtureItem>? = allStreamableByLeague[league.id]
    ) {
        val fixtures = source.orEmpty()
        val today = LocalDate.now()
        val filtered = when (tab) {
            LeagueFixtureTab.LIVE -> fixtures.filter { it.isLive() }
            LeagueFixtureTab.TODAY -> fixtures.filter { isOnDate(it, today) }
            LeagueFixtureTab.UPCOMING -> fixtures.filter { it.isUpcoming() && !isOnDate(it, today) }
        }.sortedBy { it.fixture.date }
        _leagueFixtures.value = Result.Success(filtered)
    }

    private fun applySearchFilter(query: String, source: List<StreamableLeague>) {
        val trimmed = query.trim()
        _filteredLeagues.value = if (trimmed.isBlank()) {
            source
        } else {
            val q = trimmed.lowercase()
            source.filter {
                it.name.lowercase().contains(q) ||
                    it.country.orEmpty().lowercase().contains(q)
            }
        }
    }

    private fun isOnDate(fixture: FixtureItem, date: LocalDate): Boolean {
        val dateStr = fixture.fixture.date?.take(10) ?: return false
        return runCatching {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE) == date
        }.getOrDefault(false)
    }
}
