package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.GoalStreamApp
import com.rank.football.ads.AdConstants
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.FavouritesStrip
import com.rank.football.ui.components.HeroBanner
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.PreMatchHypeCard
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
    onBrowseLeagues: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val todayMatches by viewModel.todayMatches.collectAsState()
    val favoriteFixtures by viewModel.favoriteFixtures.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val filterTeamId by viewModel.filterTeamId.collectAsState()
    val hypeFixture by viewModel.hypeFixture.collectAsState()
    val hypeH2H by viewModel.hypeH2H.collectAsState()
    val hypeHomeForm by viewModel.hypeHomeForm.collectAsState()
    val hypeAwayForm by viewModel.hypeAwayForm.collectAsState()
    val app = LocalContext.current.applicationContext as GoalStreamApp
    val catalogStatus by app.streamRepository.catalogStatus.collectAsState()
    val catalogHasStreams = catalogStatus == StreamCatalogStatus.READY
    val catalogPending = catalogStatus == StreamCatalogStatus.PENDING

    val listState = rememberLazyListState()
    val parallaxOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                400f
            }
        }
    }

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
    val filteredFavorites: Result<List<com.rank.football.data.model.FixtureItem>> = when (val r = favoriteFixtures) {
        is Result.Success -> Result.Success(filterFixtures(r.data))
        else -> r
    }

    val liveList = (liveMatches as? Result.Success)?.data.orEmpty()

    val fullyEmpty = filteredLive is Result.Success &&
        (filteredLive as Result.Success).data.isEmpty() &&
        filteredToday is Result.Success &&
        (filteredToday as Result.Success).data.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        if (fullyEmpty && liveMatches !is Result.Loading && todayMatches !is Result.Loading) {
            HeroBanner(
                fixtures = emptyList(),
                onWatchClick = onMatchClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick
            )
            if (catalogPending) {
                com.rank.football.ui.components.LoadingShimmerList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                )
            } else {
            NoStreamsEmptyState(
                context = NoStreamsContext.HOME,
                catalogHasStreams = catalogHasStreams,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                onBrowseFixtures = onBrowseFixtures,
                onBrowseLeagues = onBrowseLeagues,
                onRefresh = { viewModel.loadData() }
            )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    HeroBanner(
                        fixtures = viewModel.featuredMatches(),
                        onWatchClick = onMatchClick,
                        onSearchClick = onSearchClick,
                        onSettingsClick = onSettingsClick,
                        parallaxOffsetPx = parallaxOffset
                    )
                }

                item {
                    FavouritesStrip(
                        favorites = favorites,
                        liveFixtures = liveList,
                        selectedTeamId = filterTeamId,
                        onTeamClick = { viewModel.setFilterTeamId(it) },
                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                    )
                }

                hypeFixture?.let { hype ->
                    item {
                        PreMatchHypeCard(
                            fixture = hype,
                            h2h = hypeH2H,
                            homeForm = hypeHomeForm,
                            awayForm = hypeAwayForm,
                            onClick = { onMatchClick(hype.fixture.id) },
                            cardModifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                homeLiveSection(
                    liveMatches = filteredLive,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() },
                    catalogHasStreams = catalogHasStreams
                )

                homeFavoritesSection(
                    favoriteFixtures = filteredFavorites,
                    favoritesEmpty = favorites.isEmpty(),
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository
                )

                homeTodaySection(
                    todayMatches = filteredToday,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() },
                    catalogHasStreams = catalogHasStreams
                )
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
    }
}
