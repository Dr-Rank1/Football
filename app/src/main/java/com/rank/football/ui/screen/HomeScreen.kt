package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.GoalStreamApp
import com.rank.football.ads.AdConstants
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.FavouritesStrip
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.ProtoHeroCard
import com.rank.football.ui.components.ProtoSearchBar
import com.rank.football.ui.components.ProtoTopBar
import com.rank.football.ui.components.SectionHeader
import com.rank.football.util.Result
import com.rank.football.viewmodel.HomeViewModel
import com.rank.football.viewmodel.LeagueGroup

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onMatchClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onBrowseFixtures: () -> Unit = {},
    onBrowseLive: () -> Unit = onBrowseFixtures,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val todayMatches by viewModel.todayMatches.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val filterTeamId by viewModel.filterTeamId.collectAsState()
    val app = LocalContext.current.applicationContext as GoalStreamApp
    val catalogStatus by app.streamRepository.catalogStatus.collectAsState()
    val catalogHasStreams = catalogStatus == StreamCatalogStatus.READY
    val catalogPending = catalogStatus == StreamCatalogStatus.PENDING

    fun filterFixtures(list: List<com.rank.football.data.model.FixtureItem>) =
        if (filterTeamId == null) list else list.filter {
            it.teams.home.id == filterTeamId || it.teams.away.id == filterTeamId
        }

    val filteredLive: Result<List<com.rank.football.data.model.FixtureItem>> = when (val r = liveMatches) {
        is Result.Success -> Result.Success(filterFixtures(r.data))
        else -> r
    }
    val filteredToday: Result<List<LeagueGroup>> = when (val r = todayMatches) {
        is Result.Success -> Result.Success(
            r.data.map { g -> g.copy(fixtures = filterFixtures(g.fixtures)) }
                .filter { it.fixtures.isNotEmpty() }
        )
        else -> r
    }

    val liveList = (liveMatches as? Result.Success)?.data.orEmpty()

    val fullyEmpty = filteredLive is Result.Success &&
        (filteredLive as Result.Success).data.isEmpty() &&
        filteredToday is Result.Success &&
        (filteredToday as Result.Success).data.isEmpty()

    val featured = viewModel.featuredMatches().firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                ProtoTopBar()
            }

            item {
                ProtoSearchBar(hint = stringResource(R.string.search_hint), onClick = onSearchClick)
            }

            if (fullyEmpty && liveMatches !is Result.Loading && todayMatches !is Result.Loading) {
                if (catalogPending) {
                    item {
                        com.rank.football.ui.components.LoadingShimmerList(
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    item {
                        NoStreamsEmptyState(
                            context = NoStreamsContext.HOME,
                            catalogHasStreams = catalogHasStreams,
                            modifier = Modifier.padding(top = 8.dp),
                            onBrowseFixtures = onBrowseFixtures,
                            onRefresh = { viewModel.loadData() }
                        )
                    }
                }
            } else {
                if (featured != null) {
                    item(key = "hero_${featured.fixture.id}") {
                        ProtoHeroCard(
                            fixture = featured,
                            onWatchClick = { onMatchClick(featured.fixture.id) }
                        )
                    }
                }

                if (favorites.isNotEmpty()) {
                    item {
                        SectionHeader(label = stringResource(R.string.section_my_teams))
                    }
                    item {
                        FavouritesStrip(
                            favorites = favorites,
                            liveFixtures = liveList,
                            selectedTeamId = filterTeamId,
                            onTeamClick = { viewModel.setFilterTeamId(it) },
                            onAddClick = onSearchClick,
                            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                        )
                    }
                }

                homeLiveSection(
                    liveMatches = filteredLive,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() },
                    catalogHasStreams = catalogHasStreams,
                    onBrowseLive = onBrowseLive,
                    onBrowseFixtures = onBrowseFixtures
                )

                homeTodaySection(
                    todayMatches = filteredToday,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() },
                    catalogHasStreams = catalogHasStreams,
                    onBrowseFixtures = onBrowseFixtures
                )
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_UNIT_ID)
    }
}
