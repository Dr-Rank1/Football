package com.rank.football.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.components.EmptyStateCard
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.LeagueHeader
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.SectionTitle
import com.rank.football.util.Result
import com.rank.football.viewmodel.LeagueGroup

/** Adds the live matches horizontal carousel to the home feed. */
fun LazyListScope.homeLiveSection(
    liveMatches: Result<List<FixtureItem>>,
    onMatchClick: (Int) -> Unit,
    favoritesRepository: FavoritesRepository?,
    onRetry: () -> Unit
) {
    item {
        SectionTitle(
            title = stringResource(R.string.section_live_now),
            isLive = true
        )
    }
    when (liveMatches) {
        is Result.Loading -> item { LoadingShimmerList(count = 2) }
        is Result.Error -> item {
            ErrorState(message = stringResource(R.string.error_no_connection), onRetry = onRetry)
        }
        is Result.Success -> {
            if (liveMatches.data.isEmpty()) {
                item {
                    NoStreamsEmptyState(
                        context = NoStreamsContext.LIVE,
                        compact = true
                    )
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(liveMatches.data, key = { it.fixture.id }) { fixture ->
                            MatchCard(
                                fixture = fixture,
                                onClick = { onMatchClick(fixture.fixture.id) },
                                favoritesRepository = favoritesRepository,
                                modifier = Modifier.fillParentMaxWidth(0.88f),
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Adds favorite-team fixtures to the home feed. */
fun LazyListScope.homeFavoritesSection(
    favoriteFixtures: Result<List<FixtureItem>>,
    favoritesEmpty: Boolean,
    onMatchClick: (Int) -> Unit,
    favoritesRepository: FavoritesRepository?
) {
    if (favoritesEmpty) return
    item {
        SectionTitle(title = stringResource(R.string.section_my_teams))
    }
    when (favoriteFixtures) {
        is Result.Success -> {
            if (favoriteFixtures.data.isEmpty()) {
                item {
                    EmptyStateCard(message = stringResource(R.string.favorites_no_matches))
                }
            } else {
                items(favoriteFixtures.data, key = { "fav_${it.fixture.id}" }) { fixture ->
                    MatchCard(
                        fixture = fixture,
                        onClick = { onMatchClick(fixture.fixture.id) },
                        favoritesRepository = favoritesRepository
                    )
                }
            }
        }
        else -> item { LoadingShimmerList(count = 1) }
    }
}

/** Adds today's fixtures grouped by league to the home feed. */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.homeTodaySection(
    todayMatches: Result<List<LeagueGroup>>,
    onMatchClick: (Int) -> Unit,
    favoritesRepository: FavoritesRepository?,
    onRetry: () -> Unit
) {
    item {
        Spacer(modifier = Modifier.height(8.dp))
        SectionTitle(title = stringResource(R.string.section_today_matches))
    }
    when (todayMatches) {
        is Result.Loading -> item { LoadingShimmerList() }
        is Result.Error -> item {
            ErrorState(message = stringResource(R.string.error_no_connection), onRetry = onRetry)
        }
        is Result.Success -> {
            if (todayMatches.data.isEmpty()) {
                item {
                    NoStreamsEmptyState(
                        context = NoStreamsContext.FIXTURES,
                        compact = true
                    )
                }
            } else {
                todayMatches.data.forEach { group ->
                    stickyHeader(key = "header_${group.leagueId}") {
                        LeagueHeader(
                            leagueName = group.leagueName,
                            leagueLogo = group.leagueLogo,
                            country = group.country
                        )
                    }
                    group.fixtures.forEach { fixture ->
                        item(key = fixture.fixture.id) {
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
