package com.rank.football.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.R
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.RecentSearchEntry
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.components.EmptySearch
import com.rank.football.ui.components.EmptyStadium
import com.rank.football.ui.components.FilterPill
import com.rank.football.ui.components.FilterPillRow
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.ProtoSearchBar
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.MatchSearchFilter
import com.rank.football.viewmodel.SearchViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun SearchScreen(
    onMatchClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository(AppDatabase.getInstance(context)) }
    val query by viewModel.query.collectAsState()
    val matchFilter by viewModel.matchFilter.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState(initial = emptyList())
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val loadingSuggestions by viewModel.loadingSuggestions.collectAsState()

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
            ProtoSearchBar(
                hint = stringResource(R.string.search_hint),
                value = query,
                onValueChange = { viewModel.setQuery(it) },
                onClear = { viewModel.setQuery("") },
                onSearchSubmit = { if (query.length >= 2) viewModel.search(query) },
                autoFocus = true,
                modifier = Modifier.weight(1f)
            )
        }

        if (query.isEmpty()) {
            EmptySearch(
                onSuggestion = { term ->
                    viewModel.setQuery(term)
                    viewModel.search(term)
                }
            )
            GroupedRecentSearches(
                entries = recentSearches,
                onSelect = { viewModel.setQuery(it) },
                onRemove = { viewModel.removeRecentSearch(it) }
            )
        }

        if (query.isNotBlank()) {
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (val result = matches) {
                is Result.Loading -> item { LoadingShimmerList() }
                is Result.Success -> {
                    if (result.data.isEmpty() && query.length >= 2) {
                        item {
                            AiSuggestionsBlock(
                                loading = loadingSuggestions,
                                suggestions = aiSuggestions,
                                onPick = { term ->
                                    viewModel.setQuery(term)
                                    viewModel.search(term)
                                }
                            )
                        }
                        item {
                            EmptyStadium(
                                title = "No Matches Found",
                                subtitle = stringResource(R.string.search_no_results),
                                cta = "Clear Search",
                                onCta = { viewModel.setQuery(""); viewModel.search("") }
                            )
                        }
                    } else {
                        if (result.data.isNotEmpty() && query.isNotBlank()) {
                            item {
                                Text(
                                    text = stringResource(R.string.search_matches_count, result.data.size),
                                    color = TextGrey,
                                    fontFamily = DmSans,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                        items(result.data, key = { it.fixture.id }) { fixture ->
                            MatchCard(
                                fixture = fixture,
                                onClick = { onMatchClick(fixture.fixture.id) },
                                favoritesRepository = favoritesRepository
                            )
                        }
                    }
                }
                is Result.Error -> item { Text(result.message, color = TextWhite) }
            }
        }
    }
}

@Composable
private fun AiSuggestionsBlock(
    loading: Boolean,
    suggestions: List<String>,
    onPick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.search_no_results),
            color = TextGrey,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        when {
            loading -> {
                Text(
                    text = stringResource(R.string.search_ai_loading),
                    color = com.rank.football.ui.theme.PitchGreen,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LoadingShimmerList(count = 1)
            }
            suggestions.isNotEmpty() -> {
                Text(
                    text = stringResource(R.string.search_ai_suggestions),
                    color = TextGrey,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    suggestions.take(3).forEach { term ->
                        FilterChip(
                            selected = false,
                            onClick = { onPick(term) },
                            label = { Text(term) }
                        )
                    }
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
