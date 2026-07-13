package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.repository.FootballRepository
import com.rank.football.ui.components.LiveMatchBus
import com.rank.football.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GoalStreamApp
    private val repository = FootballRepository(application)
    private val streamRepository = app.streamRepository

    private val _liveMatches = MutableStateFlow<Result<List<FixtureItem>>>(Result.Loading)
    val liveMatches: StateFlow<Result<List<FixtureItem>>> = _liveMatches.asStateFlow()

    fun loadLiveMatches() {
        viewModelScope.launch {
            _liveMatches.value = Result.Loading
            try {
                app.syncStreamCatalog()
                val fixtures = streamRepository.filterStreamable(repository.getLiveFixtures())
                _liveMatches.value = Result.Success(fixtures)
                LiveMatchBus.publish(fixtures)
            } catch (e: Exception) {
                _liveMatches.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}
