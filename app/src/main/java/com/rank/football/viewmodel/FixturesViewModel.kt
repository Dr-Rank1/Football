package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rank.football.GoalStreamApp
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.repository.FootballRepository
import com.rank.football.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Day filter applied to streamable fixtures on the selected date. */
enum class FixtureDayFilter { ALL, LIVE, UPCOMING }

/** Calendar cell enriched with streamable match counts from real API data. */
data class CalendarDayUi(
    val date: LocalDate,
    val weekdayShort: String,
    val dayNumber: Int,
    val isToday: Boolean,
    val streamableCount: Int = 0,
    val liveCount: Int = 0
)

/** Summary stats for the currently selected calendar day. */
data class FixtureDaySummary(
    val streamableCount: Int,
    val liveCount: Int,
    val upcomingCount: Int
)

class FixturesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GoalStreamApp
    private val repository = FootballRepository(application)

    private val today = LocalDate.now()
    private val windowDays = 28

    private val _windowOffsetWeeks = MutableStateFlow(0)
    val windowOffsetWeeks: StateFlow<Int> = _windowOffsetWeeks.asStateFlow()

    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _calendarDays = MutableStateFlow<List<CalendarDayUi>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDayUi>> = _calendarDays.asStateFlow()

    private val _daySummary = MutableStateFlow(FixtureDaySummary(0, 0, 0))
    val daySummary: StateFlow<FixtureDaySummary> = _daySummary.asStateFlow()

    private val _dayFilter = MutableStateFlow(FixtureDayFilter.ALL)
    val dayFilter: StateFlow<FixtureDayFilter> = _dayFilter.asStateFlow()

    private val _fixtures = MutableStateFlow<Result<List<LeagueGroup>>>(Result.Loading)
    val fixtures: StateFlow<Result<List<LeagueGroup>>> = _fixtures.asStateFlow()

    @Volatile
    private var rawFixtures: List<FixtureItem> = emptyList()

    init {
        refreshCalendar()
    }

    /** Reloads the calendar window and fixtures for the selected date. */
    fun refreshCalendar() {
        viewModelScope.launch {
            app.syncStreamCatalog()
            loadCalendarCounts()
            loadFixtures(_selectedDate.value)
        }
    }

    /** Selects a date and loads its streamable fixtures. */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadFixtures(date)
    }

    /** Applies a live/today/upcoming filter to the selected day's fixtures. */
    fun setDayFilter(filter: FixtureDayFilter) {
        _dayFilter.value = filter
        publishFilteredFixtures()
    }

    /** Shifts the visible calendar window by one week. */
    fun shiftWeek(deltaWeeks: Int) {
        _windowOffsetWeeks.value = (_windowOffsetWeeks.value + deltaWeeks).coerceIn(-4, 8)
        viewModelScope.launch { loadCalendarCounts() }
    }

    /** Jumps back to today and reloads fixtures. */
    fun goToToday() {
        _windowOffsetWeeks.value = 0
        selectDate(today)
        viewModelScope.launch { loadCalendarCounts() }
    }

    /** Returns a formatted long label for the selected date header. */
    fun selectedDateLabel(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))

    /** Returns the month/year label for the calendar window header. */
    fun monthYearLabel(): String {
        val anchor = windowStart().plusDays(13)
        return anchor.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    private fun windowStart(): LocalDate =
        today.plusWeeks(_windowOffsetWeeks.value.toLong()).minusDays(7)

    private suspend fun loadCalendarCounts() {
        val start = windowStart()
        val days = (0 until windowDays).map { offset ->
            val date = start.plusDays(offset.toLong())
            CalendarDayUi(
                date = date,
                weekdayShort = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                dayNumber = date.dayOfMonth,
                isToday = date == today
            )
        }
        val nearbyStart = today.minusDays(1)
        val nearbyEnd = today.plusDays(1)
        val enriched = coroutineScope {
            days.map { day ->
                async {
                    if (day.date.isBefore(nearbyStart) || day.date.isAfter(nearbyEnd)) {
                        day
                    } else {
                        val fixtures = repository.getFixturesByDate(day.date)
                        day.copy(
                            streamableCount = fixtures.size,
                            liveCount = fixtures.count { it.isLive() }
                        )
                    }
                }
            }.awaitAll()
        }
        _calendarDays.value = enriched
    }

    private fun loadFixtures(date: LocalDate) {
        viewModelScope.launch {
            _fixtures.value = Result.Loading
            try {
                app.syncStreamCatalog()
                rawFixtures = repository.getFixturesByDate(date)
                _daySummary.value = FixtureDaySummary(
                    streamableCount = rawFixtures.size,
                    liveCount = rawFixtures.count { it.isLive() },
                    upcomingCount = rawFixtures.count { it.isUpcoming() }
                )
                publishFilteredFixtures()
            } catch (e: Exception) {
                _fixtures.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun publishFilteredFixtures() {
        if (rawFixtures.isEmpty() && _fixtures.value is Result.Error) return
        val filtered = when (_dayFilter.value) {
            FixtureDayFilter.ALL -> rawFixtures
            FixtureDayFilter.LIVE -> rawFixtures.filter { it.isLive() }
            FixtureDayFilter.UPCOMING -> rawFixtures.filter { it.isUpcoming() }
        }
        _fixtures.value = Result.Success(groupByLeague(filtered))
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
                    fixtures = items.sortedBy { it.fixture.date }
                )
            }
            .sortedBy { it.leagueName }
    }
}
