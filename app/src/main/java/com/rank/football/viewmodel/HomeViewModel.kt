package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.data.repository.FootballRepository
import com.rank.football.util.Result
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String?,
    val country: String?,
    val fixtures: List<FixtureItem>
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FootballRepository(application)
    private val streamRepository = (application as GoalStreamApp).streamRepository
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

    val favorites = favoritesRepository.allFavorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        loadData()
        viewModelScope.launch {
            favoritesRepository.allFavorites.collect { favs ->
                _favoriteFixtures.value = Result.Loading
                val teamIds = favs.map { it.teamId }.toSet()
                val fixtures = streamRepository.filterStreamable(repository.getFixturesForTeamIds(teamIds))
                _favoriteFixtures.value = Result.Success(fixtures)
            }
        }
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
            val fixtures = streamRepository.filterStreamable(
                repository.getLiveFixtures() + repository.getWorldCupFixtures()
            ).distinctBy { it.fixture.id }
            _liveMatches.value = Result.Success(fixtures)
        }
    }

    private fun loadToday() {
        viewModelScope.launch(exceptionHandler) {
            _todayMatches.value = Result.Loading
            val fixtures = streamRepository.filterStreamable(
                repository.getTodayFixtures() + repository.getWorldCupFixtures()
            ).distinctBy { it.fixture.id }
            _todayMatches.value = Result.Success(groupByLeague(fixtures))
        }
    }

    fun featuredMatches(): List<FixtureItem> {
        val live = (_liveMatches.value as? Result.Success)?.data.orEmpty()
        val today = (_todayMatches.value as? Result.Success)?.data
            ?.flatMap { it.fixtures }.orEmpty()
        return (live + today).distinctBy { it.fixture.id }.take(3)
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
            .sortedBy { it.leagueName }
    }
}
