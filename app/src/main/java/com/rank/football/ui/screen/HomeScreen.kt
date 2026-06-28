package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.HeroBanner
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.HomeViewModel

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

    val fullyEmpty = liveMatches is Result.Success &&
        (liveMatches as Result.Success).data.isEmpty() &&
        todayMatches is Result.Success &&
        (todayMatches as Result.Success).data.isEmpty()

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        AppScreenHeader(
            title = stringResource(R.string.home_title),
            subtitle = stringResource(R.string.home_subtitle),
            trailing = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = TextWhite)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = TextWhite)
                }
            }
        )

        if (fullyEmpty && liveMatches !is Result.Loading && todayMatches !is Result.Loading) {
            NoStreamsEmptyState(
                context = NoStreamsContext.HOME,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                onBrowseFixtures = onBrowseFixtures,
                onBrowseLeagues = onBrowseLeagues,
                onRefresh = { viewModel.loadData() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    HeroBanner(
                        fixtures = viewModel.featuredMatches(),
                        onWatchClick = onMatchClick
                    )
                }

                homeLiveSection(
                    liveMatches = liveMatches,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() }
                )

                homeFavoritesSection(
                    favoriteFixtures = favoriteFixtures,
                    favoritesEmpty = favorites.isEmpty(),
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository
                )

                homeTodaySection(
                    todayMatches = todayMatches,
                    onMatchClick = onMatchClick,
                    favoritesRepository = viewModel.favoritesRepository,
                    onRetry = { viewModel.loadData() }
                )
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
    }
}
