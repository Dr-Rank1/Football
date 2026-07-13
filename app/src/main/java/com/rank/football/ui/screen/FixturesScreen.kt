package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.isUpcoming
import com.rank.football.GoalStreamApp
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.FilterPill
import com.rank.football.ui.components.FilterPillRow
import com.rank.football.ui.components.LeagueHeader
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.SummaryCard
import com.rank.football.ui.components.UpcomingMatchSheet
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.CalendarDayUi
import com.rank.football.viewmodel.FixtureDayFilter
import com.rank.football.viewmodel.FixturesViewModel
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixturesScreen(
    onMatchClick: (Int) -> Unit,
    onBrowseLeagues: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FixturesViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val daySummary by viewModel.daySummary.collectAsState()
    val dayFilter by viewModel.dayFilter.collectAsState()
    val fixtures by viewModel.fixtures.collectAsState()
    val context = LocalContext.current
    val app = context.applicationContext as GoalStreamApp
    val catalogStatus by app.streamRepository.catalogStatus.collectAsState()
    val catalogHasStreams = catalogStatus == StreamCatalogStatus.READY
    var reminderFixture by remember { mutableStateOf<FixtureItem?>(null) }
    val calendarListState = rememberLazyListState()

    LaunchedEffect(selectedDate, calendarDays) {
        val index = calendarDays.indexOfFirst { it.date == selectedDate }
        if (index >= 0) calendarListState.animateScrollToItem(index.coerceAtLeast(0))
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppScreenHeader(
            title = stringResource(R.string.fixtures_title),
            subtitle = stringResource(R.string.fixtures_subtitle),
            trailing = {
                if (selectedDate != LocalDate.now()) {
                    TextButton(onClick = viewModel::goToToday) {
                        Text(stringResource(R.string.today), color = PitchGreen)
                    }
                }
            }
        )

        FixturesWeekNav(
            monthLabel = viewModel.monthYearLabel(),
            onPrevWeek = { viewModel.shiftWeek(-1) },
            onNextWeek = { viewModel.shiftWeek(1) }
        )

        SummaryCard(
            title = viewModel.selectedDateLabel(selectedDate),
            subtitle = stringResource(
                R.string.fixtures_day_summary,
                daySummary.streamableCount,
                daySummary.liveCount,
                daySummary.upcomingCount
            ),
            accent = daySummary.liveCount > 0
        )

        LazyRow(
            state = calendarListState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(calendarDays, key = { it.date.toString() }) { day ->
                CalendarDayCell(
                    day = day,
                    selected = day.date == selectedDate,
                    onClick = { viewModel.selectDate(day.date) }
                )
            }
        }

        FilterPillRow {
            FilterPill(
                label = stringResource(R.string.fixtures_filter_all),
                selected = dayFilter == FixtureDayFilter.ALL,
                onClick = { viewModel.setDayFilter(FixtureDayFilter.ALL) }
            )
            FilterPill(
                label = stringResource(R.string.fixtures_filter_live),
                selected = dayFilter == FixtureDayFilter.LIVE,
                onClick = { viewModel.setDayFilter(FixtureDayFilter.LIVE) }
            )
            FilterPill(
                label = stringResource(R.string.fixtures_filter_upcoming),
                selected = dayFilter == FixtureDayFilter.UPCOMING,
                onClick = { viewModel.setDayFilter(FixtureDayFilter.UPCOMING) }
            )
        }

        when (val result = fixtures) {
            is Result.Loading -> LoadingShimmerList(modifier = Modifier.weight(1f))
            is Result.Error -> ErrorState(
                message = stringResource(R.string.error_no_connection),
                onRetry = { viewModel.refreshCalendar() },
                modifier = Modifier.weight(1f)
            )
            is Result.Success -> {
                if (result.data.isEmpty()) {
                    NoStreamsEmptyState(
                        context = NoStreamsContext.FIXTURES,
                        catalogHasStreams = catalogHasStreams,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        onBrowseLeagues = onBrowseLeagues,
                        onRefresh = { viewModel.refreshCalendar() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        result.data.forEach { group ->
                            stickyHeader(key = "header_${group.leagueId}") {
                                LeagueHeader(
                                    leagueName = group.leagueName,
                                    leagueLogo = group.leagueLogo,
                                    country = group.country
                                )
                            }
                            items(group.fixtures, key = { it.fixture.id }) { fixture ->
                                MatchCard(
                                    fixture = fixture,
                                    onClick = {
                                        when {
                                            fixture.isLive() -> onMatchClick(fixture.fixture.id)
                                            fixture.isUpcoming() -> reminderFixture = fixture
                                            else -> onMatchClick(fixture.fixture.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
    }

    reminderFixture?.let { fixture ->
        UpcomingMatchSheet(
            fixture = fixture,
            onDismiss = { reminderFixture = null }
        )
    }
}

@Composable
private fun FixturesWeekNav(
    monthLabel: String,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevWeek) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = PitchGreen)
        }
        Text(
            text = monthLabel,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = PitchGreen)
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(shape)
            .background(if (selected) PitchGreen else CardDark)
            .then(
                if (day.isToday && !selected) {
                    Modifier.border(1.dp, PitchGreen.copy(alpha = 0.5f), shape)
                } else if (!selected) {
                    Modifier.border(1.dp, SurfaceDark, shape)
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.weekdayShort.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) StadiumBlack else TextGrey,
            fontSize = 10.sp
        )
        Text(
            text = day.dayNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) StadiumBlack else TextWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        when {
            day.liveCount > 0 -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LiveRed)
                )
            }
            day.streamableCount > 0 -> {
                Text(
                    text = day.streamableCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) StadiumBlack else NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
            else -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

