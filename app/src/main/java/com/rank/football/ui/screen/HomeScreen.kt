package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.GoalStreamApp
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.data.repository.StreamCatalogStatus
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.FavouritesStrip
import com.rank.football.ui.components.HeroBanner
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.PreMatchHypeCard
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
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

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        AppScreenHeader(
            title = stringResource(R.string.home_title),
            subtitle = null,
            trailing = {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(TextWhite.copy(alpha = 0.05f))
                        .border(1.dp, TextWhite.copy(alpha = 0.06f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = TextGrey, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(TextWhite.copy(alpha = 0.05f))
                        .border(1.dp, TextWhite.copy(alpha = 0.06f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = TextGrey, modifier = Modifier.size(16.dp))
                }
            }
        )

        FavouritesStrip(
            favorites = favorites,
            liveFixtures = liveList,
            selectedTeamId = filterTeamId,
            onTeamClick = { viewModel.setFilterTeamId(it) },
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
        )

        if (fullyEmpty && liveMatches !is Result.Loading && todayMatches !is Result.Loading) {
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
                        parallaxOffsetPx = parallaxOffset
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
