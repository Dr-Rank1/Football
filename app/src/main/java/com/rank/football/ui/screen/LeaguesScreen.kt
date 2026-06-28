package com.rank.football.ui.screen

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rank.football.BuildConfig
import com.rank.football.R
import com.rank.football.ads.AdConstants
import com.rank.football.ui.components.AppScreenHeader
import com.rank.football.ui.components.BannerAdView
import com.rank.football.ui.components.DetailBackHeader
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.FilterPill
import com.rank.football.ui.components.FilterPillRow
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.NoStreamsContext
import com.rank.football.ui.components.NoStreamsEmptyState
import com.rank.football.ui.components.StatPill
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.LeagueFixtureTab
import com.rank.football.viewmodel.LeagueStandingPreview
import com.rank.football.viewmodel.LeaguesViewModel
import com.rank.football.viewmodel.StreamableLeague

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaguesScreen(
    onMatchClick: (Int) -> Unit,
    onStandingsClick: (Int) -> Unit = {},
    onBrowseFixtures: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LeaguesViewModel = viewModel()
) {
    val selectedLeague by viewModel.selectedLeague.collectAsState()
    val leagueFixtures by viewModel.leagueFixtures.collectAsState()
    val fixtureTab by viewModel.fixtureTab.collectAsState()
    val standingsPreview by viewModel.standingsPreview.collectAsState()
    val filteredLeagues by viewModel.filteredLeagues.collectAsState()
    val leaguesResult by viewModel.leagues.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val liveCount by viewModel.liveStreamCount.collectAsState()
    val todayCount by viewModel.todayStreamCount.collectAsState()
    val catalogConfigured by viewModel.catalogConfigured.collectAsState()
    val overviewLoading by viewModel.overviewLoading.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        if (selectedLeague == null) {
            LeaguesOverview(
                modifier = Modifier.weight(1f),
                leaguesResult = leaguesResult,
                filteredLeagues = filteredLeagues,
                searchQuery = searchQuery,
                liveCount = liveCount,
                todayCount = todayCount,
                catalogConfigured = catalogConfigured,
                overviewLoading = overviewLoading,
                onSearchChange = viewModel::setSearchQuery,
                onLeagueClick = viewModel::selectLeague,
                onLeagueLongClick = onStandingsClick,
                onRetry = viewModel::loadOverview,
                onBrowseFixtures = onBrowseFixtures
            )
        } else {
            LeagueDetailView(
                modifier = Modifier.weight(1f),
                league = selectedLeague!!,
                fixtureTab = fixtureTab,
                leagueFixtures = leagueFixtures,
                standingsPreview = standingsPreview,
                onBack = viewModel::clearSelection,
                onStandings = { onStandingsClick(selectedLeague!!.id) },
                onTabChange = viewModel::setFixtureTab,
                onMatchClick = onMatchClick,
                onRetry = { viewModel.selectLeague(selectedLeague!!) },
                onBrowseFixtures = onBrowseFixtures
            )
        }

        BannerAdView(adUnitId = AdConstants.BANNER_TEST_UNIT_ID)
    }
}

@Composable
private fun LeaguesOverview(
    modifier: Modifier = Modifier,
    leaguesResult: Result<List<StreamableLeague>>,
    filteredLeagues: List<StreamableLeague>,
    searchQuery: String,
    liveCount: Int,
    todayCount: Int,
    catalogConfigured: Boolean,
    overviewLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onLeagueClick: (StreamableLeague) -> Unit,
    onLeagueLongClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onBrowseFixtures: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppScreenHeader(
            title = stringResource(R.string.leagues_title),
            subtitle = stringResource(R.string.leagues_subtitle)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatPill(
                label = stringResource(R.string.leagues_stat_live, liveCount),
                highlight = liveCount > 0,
                modifier = Modifier.weight(1f)
            )
            StatPill(
                label = stringResource(R.string.leagues_stat_today, todayCount),
                highlight = todayCount > 0,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.leagues_search_hint), color = TextGrey) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = PitchGreen)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PitchGreen,
                unfocusedBorderColor = SurfaceDark,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = PitchGreen
            )
        )

        if (!catalogConfigured && !overviewLoading && BuildConfig.DEBUG) {
            Text(
                text = stringResource(R.string.leagues_catalog_missing),
                color = TextGrey,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        when (leaguesResult) {
            is Result.Loading -> LoadingShimmerList(modifier = Modifier.weight(1f))
            is Result.Error -> ErrorState(
                message = stringResource(R.string.error_no_connection),
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
            is Result.Success -> {
                if (filteredLeagues.isEmpty()) {
                    NoStreamsEmptyState(
                        context = NoStreamsContext.LEAGUES,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        onBrowseFixtures = onBrowseFixtures,
                        onRefresh = onRetry
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredLeagues, key = { it.id }) { league ->
                            StreamableLeagueRow(
                                league = league,
                                onClick = { onLeagueClick(league) },
                                onLongClick = { onLeagueLongClick(league.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StreamableLeagueRow(
    league: StreamableLeague,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardDark)
            .border(1.dp, SurfaceDark, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = league.logo,
            contentDescription = league.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(StadiumBlack),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = league.name,
                color = TextWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!league.countryFlag.isNullOrBlank()) {
                    AsyncImage(
                        model = league.countryFlag,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = league.country.orEmpty(),
                    color = TextGrey,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (league.liveCount > 0) {
                LeagueCountBadge(
                    text = stringResource(R.string.leagues_live_badge, league.liveCount),
                    color = LiveRed
                )
            }
            if (league.todayCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                LeagueCountBadge(
                    text = stringResource(R.string.leagues_today_badge, league.todayCount),
                    color = PitchGreen
                )
            }
        }
    }
}

@Composable
private fun LeagueCountBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun LeagueDetailView(
    modifier: Modifier = Modifier,
    league: StreamableLeague,
    fixtureTab: LeagueFixtureTab,
    leagueFixtures: Result<List<com.rank.football.data.model.FixtureItem>>,
    standingsPreview: List<LeagueStandingPreview>,
    onBack: () -> Unit,
    onStandings: () -> Unit,
    onTabChange: (LeagueFixtureTab) -> Unit,
    onMatchClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onBrowseFixtures: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailBackHeader(
            title = league.name,
            onBack = onBack,
            actionLabel = stringResource(R.string.standings_title),
            onAction = onStandings
        )

        LeagueHeaderCard(league = league)

        if (standingsPreview.isNotEmpty()) {
            StandingsPreviewTable(rows = standingsPreview, onViewAll = onStandings)
        }

        FilterPillRow {
            FilterPill(
                label = stringResource(R.string.leagues_tab_live),
                selected = fixtureTab == LeagueFixtureTab.LIVE,
                onClick = { onTabChange(LeagueFixtureTab.LIVE) }
            )
            FilterPill(
                label = stringResource(R.string.leagues_tab_today),
                selected = fixtureTab == LeagueFixtureTab.TODAY,
                onClick = { onTabChange(LeagueFixtureTab.TODAY) }
            )
            FilterPill(
                label = stringResource(R.string.leagues_tab_upcoming),
                selected = fixtureTab == LeagueFixtureTab.UPCOMING,
                onClick = { onTabChange(LeagueFixtureTab.UPCOMING) }
            )
        }

        when (leagueFixtures) {
            is Result.Loading -> LoadingShimmerList(modifier = Modifier.weight(1f))
            is Result.Error -> ErrorState(
                message = stringResource(R.string.error_no_connection),
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
            is Result.Success -> {
                if (leagueFixtures.data.isEmpty()) {
                    NoStreamsEmptyState(
                        context = NoStreamsContext.LEAGUES,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        onBrowseFixtures = onBrowseFixtures,
                        onRefresh = onRetry
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(leagueFixtures.data, key = { it.fixture.id }) { fixture ->
                            MatchCard(
                                fixture = fixture,
                                onClick = { onMatchClick(fixture.fixture.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueHeaderCard(league: StreamableLeague) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceDark, RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = league.logo,
            contentDescription = league.name,
            modifier = Modifier
                .size(56.dp)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = league.name,
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.leagues_season_label, league.season),
                color = TextGrey,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(
                    R.string.leagues_stream_summary,
                    league.liveCount,
                    league.todayCount,
                    league.upcomingCount
                ),
                color = NeonGreen,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StandingsPreviewTable(
    rows: List<LeagueStandingPreview>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceDark, RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.leagues_top_table),
                color = TextWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAll) {
                Text(stringResource(R.string.leagues_view_standings), color = PitchGreen)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text("#", color = TextGrey, modifier = Modifier.width(24.dp))
            Text(
                stringResource(R.string.standings_club),
                color = TextGrey,
                modifier = Modifier.weight(1f)
            )
            Text("P", color = TextGrey, modifier = Modifier.width(24.dp))
            Text("Pts", color = TextGrey, modifier = Modifier.width(32.dp))
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.rank.toString(), color = TextWhite, modifier = Modifier.width(24.dp))
                AsyncImage(
                    model = row.teamLogo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    row.teamName,
                    color = TextWhite,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(row.played.toString(), color = TextGrey, modifier = Modifier.width(24.dp))
                Text(
                    row.points.toString(),
                    color = PitchGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}
