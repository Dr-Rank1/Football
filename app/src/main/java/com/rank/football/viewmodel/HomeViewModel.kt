package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.OnboardingPreference
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isFinished
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.data.repository.FootballRepository
import com.rank.football.ui.components.HypeH2H
import com.rank.football.ui.components.LiveMatchBus
import com.rank.football.util.Result
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class LeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String?,
    val country: String?,
    val fixtures: List<FixtureItem>
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FootballRepository(application)
    val favoritesRepository = FavoritesRepository(AppDatabase.getInstance(application))

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _liveMatches.value = Result.Error(throwable.message ?: "Unknown error")
    }

    private val _liveMatches = MutableStateFlow<Result<List<FixtureItem>>>(Result.Loading)
    val liveMatches: StateFlow<Result<List<FixtureItem>>> = _liveMatches.asStateFlow()

    private val _todayMatches = MutableStateFlow<Result<List<LeagueGroup>>>(Result.Loading)
    val todayMatches: StateFlow<Result<List<LeagueGroup>>> = _todayMatches.asStateFlow()

    private val _favoriteFixtures = MutableStateFlow<Result<List<FixtureItem>>>(Result.Loading)
    val favoriteFixtures: StateFlow<Result<List<FixtureItem>>> = _favoriteFixtures.asStateFlow()

    private val _filterTeamId = MutableStateFlow<Int?>(null)
    val filterTeamId: StateFlow<Int?> = _filterTeamId.asStateFlow()

    private val _hypeFixture = MutableStateFlow<FixtureItem?>(null)
    val hypeFixture: StateFlow<FixtureItem?> = _hypeFixture.asStateFlow()

    private val _hypeH2H = MutableStateFlow<HypeH2H?>(null)
    val hypeH2H: StateFlow<HypeH2H?> = _hypeH2H.asStateFlow()

    private val _hypeHomeForm = MutableStateFlow("")
    val hypeHomeForm: StateFlow<String> = _hypeHomeForm.asStateFlow()

    private val _hypeAwayForm = MutableStateFlow("")
    val hypeAwayForm: StateFlow<String> = _hypeAwayForm.asStateFlow()

    val favorites = favoritesRepository.allFavorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var preferredLeagueIds: Set<Int> = emptySet()

    init {
        loadData()
        viewModelScope.launch {
            OnboardingPreference.preferredLeagueIds(getApplication()).collect { ids ->
                preferredLeagueIds = ids
                // Re-sort today's groups when prefs arrive
                val current = _todayMatches.value
                if (current is Result.Success) {
                    _todayMatches.value = Result.Success(
                        current.data.sortedWith(
                            compareByDescending<LeagueGroup> { it.leagueId in preferredLeagueIds }
                                .thenBy { it.leagueName }
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            favoritesRepository.allFavorites.collect { favs ->
                _favoriteFixtures.value = Result.Loading
                val teamIds = favs.map { it.teamId }.toSet()
                val fixtures = repository.getFixturesForTeamIds(teamIds)
                _favoriteFixtures.value = Result.Success(fixtures)
            }
        }
    }

    fun setFilterTeamId(teamId: Int?) {
        _filterTeamId.value = teamId
    }

    fun matchesTeamFilter(fixture: FixtureItem): Boolean {
        val id = _filterTeamId.value ?: return true
        return fixture.teams.home.id == id || fixture.teams.away.id == id
    }

    fun loadData() {
        viewModelScope.launch(exceptionHandler) {
            (getApplication<Application>() as GoalStreamApp).syncStreamCatalog()
            loadLive()
            loadToday()
        }
    }

    private fun loadLive() {
        viewModelScope.launch(exceptionHandler) {
            _liveMatches.value = Result.Loading
            val fixtures = repository.getLiveFixtures().distinctBy { it.fixture.id }
            _liveMatches.value = Result.Success(fixtures)
            LiveMatchBus.publish(fixtures)
        }
    }

    private fun loadToday() {
        viewModelScope.launch(exceptionHandler) {
            _todayMatches.value = Result.Loading
            val today = LocalDate.now()
            val weekendEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            val fixtures = repository.getFixturesInRange(today, weekendEnd)
                .distinctBy { it.fixture.id }
            _todayMatches.value = Result.Success(groupByLeague(fixtures))
            loadHypeCard(fixtures)
        }
    }

    private fun loadHypeCard(fixtures: List<FixtureItem>) {
        viewModelScope.launch(exceptionHandler) {
            val upcoming = fixtures
                .filter { it.isUpcoming() }
                .mapNotNull { f ->
                    val ms = runCatching {
                        Instant.parse(f.fixture.date).toEpochMilli() - System.currentTimeMillis()
                    }.getOrNull() ?: return@mapNotNull null
                    f to ms
                }
                .filter { it.second in 0..(90 * 60 * 1000L) }
                .minByOrNull { it.second }
                ?.first

            _hypeFixture.value = upcoming
            if (upcoming == null) {
                _hypeH2H.value = null
                _hypeHomeForm.value = ""
                _hypeAwayForm.value = ""
                return@launch
            }

            val homeId = upcoming.teams.home.id
            val awayId = upcoming.teams.away.id
            if (homeId != null && awayId != null) {
                val h2h = repository.getHeadToHead(homeId, awayId)
                var hw = 0; var d = 0; var aw = 0
                h2h.filter { it.isFinished() }.forEach { m ->
                    val hg = m.goals.home ?: return@forEach
                    val ag = m.goals.away ?: return@forEach
                    when {
                        hg > ag && m.teams.home.id == homeId -> hw++
                        ag > hg && m.teams.away.id == homeId -> hw++
                        hg > ag && m.teams.home.id == awayId -> aw++
                        ag > hg && m.teams.away.id == awayId -> aw++
                        hg == ag -> d++
                    }
                }
                _hypeH2H.value = HypeH2H(hw, d, aw)
                val season = upcoming.league.season ?: java.time.Year.now().value
                _hypeHomeForm.value = repository.getTeamForm(homeId, upcoming.league.id, season)
                    .takeIf { it != "N/A" }.orEmpty()
                _hypeAwayForm.value = repository.getTeamForm(awayId, upcoming.league.id, season)
                    .takeIf { it != "N/A" }.orEmpty()
            }
        }
    }

    fun featuredMatches(): List<FixtureItem> {
        val live = (_liveMatches.value as? Result.Success)?.data.orEmpty()
            .filter { matchesTeamFilter(it) }
        val today = (_todayMatches.value as? Result.Success)?.data
            ?.flatMap { it.fixtures }.orEmpty()
            .filter { matchesTeamFilter(it) }
        val hype = _hypeFixture.value
        // Prefer live; if none and hype is live-transitioning, include it once kickoff hits
        return (live + today).distinctBy { it.fixture.id }.take(3).ifEmpty {
            listOfNotNull(hype)
        }
    }

    private fun groupByLeague(fixtures: List<FixtureItem>): List<LeagueGroup> {
        return fixtures
            .groupBy { it.league.id }
            .map { (_, items) ->
                val league = items.first().league
                LeagueGroup(
                    leagueId = league.id,
                    leagueName = league.name,
                    leagueLogo = league.logo,
                    country = league.country,
                    fixtures = items
                )
            }
            .sortedWith(
                compareByDescending<LeagueGroup> { it.leagueId in preferredLeagueIds }
                    .thenBy { it.leagueName }
            )
    }
}
