package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.GoalStreamApp
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.LeagueHeader
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.SectionTitle
import com.rank.football.util.Result
import com.rank.football.viewmodel.LiveViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveScreen(
    onMatchClick: (Int) -> Unit,
    onLiveCountChanged: (Int) -> Unit,
    onLiveMatchesChanged: (List<com.rank.football.data.model.FixtureItem>) -> Unit = {},
    onBrowseFixtures: () -> Unit = {},
    onBrowseLeagues: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LiveViewModel = viewModel()
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(AppDatabase.getInstance(context)) }
    val app = context.applicationContext as GoalStreamApp
    val catalogStatus by app.streamRepository.catalogStatus.collectAsState()
    val catalogHasStreams = catalogStatus == StreamCatalogStatus.READY
    val catalogPending = catalogStatus == StreamCatalogStatus.PENDING

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

    val liveSubtitle = when (val result = liveMatches) {
        is Result.Success -> "${result.data.size} ${stringResource(R.string.live_subtitle)}"
        else -> null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppScreenHeader(
            title = stringResource(R.string.live_title),
            subtitle = liveSubtitle
        )

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
                    NoStreamsEmptyState(
                        context = NoStreamsContext.LIVE,
                        catalogHasStreams = catalogHasStreams,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        onBrowseFixtures = onBrowseFixtures,
                        onBrowseLeagues = onBrowseLeagues,
                        onRefresh = { viewModel.loadLiveMatches() }
                    )
                    }
                } else {
                    val featured = result.data.first()
                    val grouped = remember(result.data) {
                        groupLiveMatchesByLeague(result.data.drop(1))
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        item(key = "featured_${featured.fixture.id}") {
                            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                                SectionTitle(title = "FEATURED", isLive = true)
                                MatchCard(
                                    fixture = featured,
                                    onClick = { onMatchClick(featured.fixture.id) },
                                    favoritesRepository = favoritesRepository,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                        grouped.forEach { group ->
                            stickyHeader(key = "header_${group.leagueId}") {
                                if (group.leagueLogo != null) {
                                    LeagueHeader(
                                        leagueName = group.leagueName,
                                        leagueLogo = group.leagueLogo,
                                        country = group.country
                                    )
                                } else {
                                    SectionTitle(title = group.leagueName, isLive = true)
                                }
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

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
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
