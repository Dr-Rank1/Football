package com.rank.football.ui.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.util.Result
import com.rank.football.viewmodel.LiveViewModel
import kotlinx.coroutines.delay

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

    Column(modifier = modifier.fillMaxSize()) {
        AppScreenHeader(
            title = stringResource(R.string.live_title),
            subtitle = stringResource(R.string.live_subtitle)
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
                    NoStreamsEmptyState(
                        context = NoStreamsContext.LIVE,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        onBrowseFixtures = onBrowseFixtures,
                        onBrowseLeagues = onBrowseLeagues,
                        onRefresh = { viewModel.loadLiveMatches() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(result.data, key = { it.fixture.id }) { fixture ->
                            MatchCard(
                                fixture = fixture,
                                onClick = { onMatchClick(fixture.fixture.id) }
                            )
                        }
                    }
                }
            }
        }

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
    }
}
