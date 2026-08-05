package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isFinished
import com.rank.football.data.model.isLive
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.GoalStreamApp
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.EmptyStadium
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.LeagueHeader
import com.rank.football.ui.components.LiveCountBanner
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.ProtoPill
import com.rank.football.ui.components.SectionHeader
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.LiveViewModel
import kotlinx.coroutines.delay

private enum class LiveFilter(val label: String) {
    ALL("ALL"), LIVE("LIVE"), FINISHED("FINISHED")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveScreen(
    onMatchClick: (Int) -> Unit,
    onLiveCountChanged: (Int) -> Unit,
    onLiveMatchesChanged: (List<com.rank.football.data.model.FixtureItem>) -> Unit = {},
    onBrowseFixtures: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LiveViewModel = viewModel()
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val todayUpcoming by viewModel.todayUpcoming.collectAsState()
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(AppDatabase.getInstance(context)) }
    val app = context.applicationContext as GoalStreamApp
    val catalogStatus by app.streamRepository.catalogStatus.collectAsState()
    val catalogPending = catalogStatus == StreamCatalogStatus.PENDING
    var filter by remember { mutableStateOf(LiveFilter.ALL) }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadLiveMatches()
            delay(60_000)
        }
    }

    LaunchedEffect(liveMatches) {
        if (liveMatches is Result.Success) {
            val data = (liveMatches as Result.Success).data
            onLiveCountChanged(data.size)
            onLiveMatchesChanged(data)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StadiumBlack)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "LIVE SCORES",
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveFilter.entries.forEach { f ->
                    ProtoPill(
                        label = f.label,
                        selected = filter == f,
                        onClick = { filter = f }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        when (val result = liveMatches) {
            is Result.Loading -> {
                LoadingShimmerList(modifier = Modifier.weight(1f))
            }
            is Result.Error -> {
                ErrorState(
                    message = stringResource(R.string.error_no_connection),
                    modifier = Modifier.weight(1f),
                    onRetry = { viewModel.loadLiveMatches() }
                )
            }
            is Result.Success -> {
                if (result.data.isEmpty()) {
                    if (catalogPending) {
                        LoadingShimmerList(modifier = Modifier.weight(1f))
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            EmptyStadium(
                                title = stringResource(R.string.live_empty_title),
                                subtitle = stringResource(R.string.live_empty_subtitle),
                                cta = stringResource(R.string.live_empty_cta),
                                onCta = onBrowseFixtures
                            )
                            if (todayUpcoming.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                SectionHeader(
                                    label = stringResource(R.string.live_up_next),
                                    count = todayUpcoming.size,
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                                )
                                todayUpcoming.take(3).forEach { fixture ->
                                    MatchCard(
                                        fixture = fixture,
                                        onClick = { onMatchClick(fixture.fixture.id) },
                                        favoritesRepository = favoritesRepository
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                } else {
                    val data = result.data
                    val filtered = when (filter) {
                        LiveFilter.ALL -> data
                        LiveFilter.LIVE -> data.filter { it.isLive() }
                        LiveFilter.FINISHED -> data.filter { it.isFinished() }
                    }

                    if (filter != LiveFilter.FINISHED) {
                        LiveCountBanner(count = data.size)
                    }

                    if (filtered.isEmpty()) {
                        EmptyStadium(
                            title = "No finished matches",
                            subtitle = "Check back after full time",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val grouped = remember(data, filter) {
                            groupLiveMatchesByLeague(filtered)
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            grouped.forEach { group ->
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
                                        onClick = { onMatchClick(fixture.fixture.id) },
                                        favoritesRepository = favoritesRepository
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_UNIT_ID)
    }
}

private data class LiveLeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val leagueLogo: String?,
    val country: String?,
    val fixtures: List<FixtureItem>
)

private fun groupLiveMatchesByLeague(matches: List<FixtureItem>): List<LiveLeagueGroup> =
    matches
        .groupBy { it.league.id }
        .map { (_, fixtures) ->
            val league = fixtures.first().league
            LiveLeagueGroup(
                leagueId = league.id,
                leagueName = league.name,
                leagueLogo = league.logo,
                country = league.country,
                fixtures = fixtures
            )
        }
        .sortedBy { it.leagueName }
