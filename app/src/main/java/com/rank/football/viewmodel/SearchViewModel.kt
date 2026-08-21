package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.ai.SearchSuggestionEngine
import com.rank.football.data.local.OnboardingPreference
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.repository.FootballRepository
import com.rank.football.util.Result
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** Client-side match filter applied to search results. */
enum class MatchSearchFilter { ALL, LIVE, TODAY, WEEK }

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FootballRepository(application)
    private val context = application.applicationContext
    private val suggestionEngine = SearchSuggestionEngine()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _matchFilter = MutableStateFlow(MatchSearchFilter.ALL)
    val matchFilter: StateFlow<MatchSearchFilter> = _matchFilter.asStateFlow()

    private val _matches = MutableStateFlow<Result<List<FixtureItem>>>(Result.Success(emptyList()))
    val matches: StateFlow<Result<List<FixtureItem>>> = _matches.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()

    private val _loadingSuggestions = MutableStateFlow(false)
    val loadingSuggestions: StateFlow<Boolean> = _loadingSuggestions.asStateFlow()

    private var lastMatchResults: List<FixtureItem> = emptyList()

    val recentSearches = OnboardingPreference.recentSearchesWithTimestamps(context)

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _matches.value = Result.Error("Search failed")
    }

    init {
        observeQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _query.debounce(400).distinctUntilChanged().collect { q ->
                if (q.length >= 2) search(q) else clearResults()
            }
        }
    }

    /** Updates the active search query text. */
    fun setQuery(value: String) {
        _query.value = value
        _aiSuggestions.value = emptyList()
    }

    /** Applies a client-side match filter to search results. */
    fun setMatchFilter(filter: MatchSearchFilter) {
        _matchFilter.value = filter
        if (lastMatchResults.isNotEmpty()) {
            _matches.value = Result.Success(applyMatchFilter(lastMatchResults))
        }
    }

    /** Removes a single term from recent search history. */
    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            OnboardingPreference.removeRecentSearch(context, query)
        }
    }

    private fun clearResults() {
        _matches.value = Result.Success(emptyList())
        _aiSuggestions.value = emptyList()
    }

    /** Runs match search for the query. */
    fun search(q: String) {
        viewModelScope.launch(exceptionHandler) {
            OnboardingPreference.addRecentSearch(context, q)
            _matches.value = Result.Loading
            (getApplication<Application>() as GoalStreamApp).syncStreamCatalog()
            lastMatchResults = repository.searchFixtures(q)
            val results = applyMatchFilter(lastMatchResults)
            _matches.value = Result.Success(results)
            if (results.isEmpty()) loadAiSuggestions(q)
        }
    }

    /** Fetches Gemini alternative search suggestions when results are empty. */
    private suspend fun loadAiSuggestions(q: String) {
        _loadingSuggestions.value = true
        _aiSuggestions.value = suggestionEngine.suggest(q)
        _loadingSuggestions.value = false
    }

    /** Filters match results by live/today/week criteria. */
    private fun applyMatchFilter(items: List<FixtureItem>): List<FixtureItem> {
        val today = LocalDate.now(ZoneId.systemDefault())
        return when (_matchFilter.value) {
            MatchSearchFilter.ALL -> items
            MatchSearchFilter.LIVE -> items.filter { it.isLive() }
            MatchSearchFilter.TODAY -> items.filter {
                it.fixture.date?.startsWith(today.toString()) == true
            }
            MatchSearchFilter.WEEK -> items.filter {
                val dateStr = it.fixture.date?.take(10) ?: return@filter false
                val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return@filter false
                !date.isBefore(today) && !date.isAfter(today.plusDays(7))
            }
        }
    }
}
