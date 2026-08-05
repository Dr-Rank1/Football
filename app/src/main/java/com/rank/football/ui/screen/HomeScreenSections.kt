package com.rank.football.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.R
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.components.ErrorState
import com.rank.football.ui.components.LeagueHeader
import com.rank.football.ui.components.LoadingShimmerList
import com.rank.football.ui.components.MatchCard
import com.rank.football.ui.components.SectionHeader
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.LeagueGroup

/** Adds the live matches section to the home feed. */
fun LazyListScope.homeLiveSection(
    liveMatches: Result<List<FixtureItem>>,
    onMatchClick: (Int) -> Unit,
    favoritesRepository: FavoritesRepository?,
    onRetry: () -> Unit,
    catalogHasStreams: Boolean = true,
    onBrowseLive: () -> Unit = {},
    onBrowseFixtures: () -> Unit = {}
) {
    when (liveMatches) {
        is Result.Loading -> item { LoadingShimmerList(count = 2) }
        is Result.Error -> item {
            ErrorState(message = stringResource(R.string.error_no_connection), onRetry = onRetry)
        }
        is Result.Success -> {
            item {
                SectionHeader(
                    label = "Live Now",
                    count = liveMatches.data.size,
                    action = "See all",
                    onAction = onBrowseLive,
                    contentPadding = PaddingValuesHome()
                )
            }
            if (liveMatches.data.isEmpty()) {
                item {
                    HomeInlineEmptyCard(
                        icon = Icons.Default.DarkMode,
                        title = "No Live Matches",
                        subtitle = "Check back at kick-off time",
                        cta = "View Fixtures →",
                        onCta = onBrowseFixtures
                    )
                }
            } else {
                items(liveMatches.data.take(3), key = { it.fixture.id }) { fixture ->
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

/** Adds today's fixtures grouped by league to the home feed. */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.homeTodaySection(
    todayMatches: Result<List<LeagueGroup>>,
    onMatchClick: (Int) -> Unit,
    favoritesRepository: FavoritesRepository?,
    onRetry: () -> Unit,
    catalogHasStreams: Boolean = true,
    onBrowseFixtures: () -> Unit = {}
) {
    item {
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader(
            label = "Today's Fixtures",
            action = "See all",
            onAction = onBrowseFixtures,
            contentPadding = PaddingValuesHome()
        )
    }
    when (todayMatches) {
        is Result.Loading -> item { LoadingShimmerList() }
        is Result.Error -> item {
            ErrorState(message = stringResource(R.string.error_no_connection), onRetry = onRetry)
        }
        is Result.Success -> {
            if (todayMatches.data.isEmpty()) {
                item {
                    HomeInlineEmptyCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Rest Day",
                        subtitle = "No matches scheduled today — check the week ahead",
                        cta = "View Fixtures →",
                        onCta = onBrowseFixtures
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

/** Inline section empty card used on the home feed (UI reference). */
@Composable
private fun HomeInlineEmptyCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    cta: String? = null,
    onCta: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PitchGreen.copy(alpha = 0.08f),
                        CardDark.copy(alpha = 0.5f)
                    )
                )
            )
            .background(CardDark)
            .border(1.dp, PitchGreen.copy(alpha = 0.14f), shape)
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SurfaceDark.copy(alpha = 0.6f))
                .border(1.dp, TextWhite.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PitchGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title.uppercase(),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = TextGrey,
            fontFamily = DmSans,
            fontSize = 12.sp
        )
        if (cta != null && onCta != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PitchGreen.copy(alpha = 0.12f))
                    .border(1.dp, PitchGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .clickable(onClick = onCta)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cta,
                    color = PitchGreen,
                    fontFamily = DmSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        MiniPitchLine(modifier = Modifier.fillMaxWidth(0.55f))
    }
}

/** Decorative half-pitch line under inline empty cards. */
@Composable
private fun MiniPitchLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(10.dp)) {
        val stroke = Stroke(width = 1.2.dp.toPx())
        val color = PitchGreen.copy(alpha = 0.22f)
        val h = size.height
        drawLine(color, Offset(0f, h / 2f), Offset(size.width, h / 2f), stroke.width)
        drawCircle(color, radius = size.height * 0.4f, center = Offset(size.width / 2f, h / 2f), style = stroke)
        drawLine(color, Offset(size.width / 2f, 0f), Offset(size.width / 2f, h), stroke.width)
        drawOval(color = PitchGreen.copy(alpha = 0.1f), topLeft = Offset(size.width * 0.4f, h * 0.3f), size = Size(size.width * 0.2f, h * 0.4f), style = stroke)
        drawOval(color = PitchGreen.copy(alpha = 0.1f), topLeft = Offset(0f, h * 0.3f), size = Size(size.width * 0.14f, h * 0.4f), style = stroke)
        drawOval(color = PitchGreen.copy(alpha = 0.1f), topLeft = Offset(size.width * 0.86f, h * 0.3f), size = Size(size.width * 0.14f, h * 0.4f), style = stroke)
    }
}

private fun PaddingValuesHome() =
    androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
