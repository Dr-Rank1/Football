package com.rank.football.ui.screen

import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rank.football.ui.theme.TextWhite
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rank.football.R
import com.rank.football.data.local.RecentSearchEntry
import com.rank.football.ui.components.FilterPill
import com.rank.football.ui.components.FilterPillRow
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.MatchSearchFilter
import com.rank.football.viewmodel.SearchTab
import com.rank.football.viewmodel.SearchViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMatchClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val matchFilter by viewModel.matchFilter.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val leagues by viewModel.leagues.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState(initial = emptyList())
    val trending by viewModel.trending.collectAsState()
    var active by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextWhite)
            }
            SearchBar(
                query = query,
                onQueryChange = { viewModel.setQuery(it) },
                onSearch = { viewModel.search(it) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PitchGreen)
                },
                modifier = Modifier.weight(1f)
            ) {}
        }

        if (query.isEmpty()) {
            if (trending.isNotEmpty()) {
                Text(
                    stringResource(R.string.search_trending),
                    color = TextGrey,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(trending) { term ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.setQuery(term); viewModel.search(term) },
                            label = { Text(term) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
            GroupedRecentSearches(
                entries = recentSearches,
                onSelect = { viewModel.setQuery(it) },
                onRemove = { viewModel.removeRecentSearch(it) }
            )
        }

        if (selectedTab == SearchTab.MATCHES) {
            FilterPillRow {
                MatchSearchFilter.entries.forEach { filter ->
                    FilterPill(
                        label = when (filter) {
                            MatchSearchFilter.ALL -> stringResource(R.string.search_filter_all)
                            MatchSearchFilter.LIVE -> stringResource(R.string.search_filter_live)
                            MatchSearchFilter.TODAY -> stringResource(R.string.search_filter_today)
                            MatchSearchFilter.WEEK -> stringResource(R.string.search_filter_week)
                        },
                        selected = matchFilter == filter,
                        onClick = { viewModel.setMatchFilter(filter) }
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = StadiumBlack,
            contentColor = PitchGreen
        ) {
            Tab(
                selected = selectedTab == SearchTab.TEAMS,
                onClick = { viewModel.setTab(SearchTab.TEAMS) },
                text = { Text(stringResource(R.string.search_tab_teams)) }
            )
            Tab(
                selected = selectedTab == SearchTab.LEAGUES,
                onClick = { viewModel.setTab(SearchTab.LEAGUES) },
                text = { Text(stringResource(R.string.search_tab_leagues)) }
            )
            Tab(
                selected = selectedTab == SearchTab.MATCHES,
                onClick = { viewModel.setTab(SearchTab.MATCHES) },
                text = { Text(stringResource(R.string.search_tab_matches)) }
            )
        }


        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                SearchTab.TEAMS -> when (val result = teams) {
                    is Result.Loading -> item { LoadingShimmerList() }
                    is Result.Success -> items(result.data) { item ->
                        SearchResultRow(
                            logo = item.team.logo,
                            title = item.team.name,
                            subtitle = item.venue?.city
                        )
                    }
                    is Result.Error -> item { Text(result.message, color = TextWhite) }
                }
                SearchTab.LEAGUES -> when (val result = leagues) {
                    is Result.Loading -> item { LoadingShimmerList() }
                    is Result.Success -> items(result.data) { item ->
                        SearchResultRow(
                            logo = item.league.logo,
                            title = item.league.name,
                            subtitle = item.country?.name
                        )
                    }
                    is Result.Error -> item { Text(result.message, color = TextWhite) }
                }
                SearchTab.MATCHES -> when (val result = matches) {
                    is Result.Loading -> item { LoadingShimmerList() }
                    is Result.Success -> items(result.data, key = { it.fixture.id }) { fixture ->
                        MatchCard(
                            fixture = fixture,
                            onClick = { onMatchClick(fixture.fixture.id) }
                        )
                    }
                    is Result.Error -> item { Text(result.message, color = TextWhite) }
                }
            }
        }
    }
}

/** Groups recent searches into Today, Yesterday, and Earlier sections. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupedRecentSearches(
    entries: List<RecentSearchEntry>,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    if (entries.isEmpty()) return
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    val grouped = linkedMapOf<String, List<RecentSearchEntry>>()
    entries.forEach { entry ->
        val date = Instant.ofEpochMilli(entry.timestampMs).atZone(zone).toLocalDate()
        val label = when (date) {
            today -> stringResource(R.string.today)
            yesterday -> stringResource(R.string.search_yesterday)
            else -> stringResource(R.string.search_earlier)
        }
        grouped[label] = grouped.getOrDefault(label, emptyList()) + entry
    }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        grouped.forEach { (label, group) ->
            Text(label, color = TextGrey, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            group.forEach { entry ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onRemove(entry.query)
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(StadiumBlack).padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete), tint = TextGrey)
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(entry.query) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entry.query, color = TextWhite, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(logo: String?, title: String, subtitle: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, SurfaceDark, RoundedCornerShape(14.dp))
            .clickable { }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = logo,
            contentDescription = title,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Fit
        )
        Column {
            Text(title, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            subtitle?.let {
                Text(it, color = PitchGreen, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
