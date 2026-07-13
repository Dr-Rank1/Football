package com.rank.football.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val HERO_STADIUM =
    "https://images.unsplash.com/photo-1569531955323-33c6b2dca44b?w=800&h=560&fit=crop&auto=format"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    fixtures: List<FixtureItem>,
    onWatchClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    parallaxOffsetPx: Float = 0f
) {
    if (fixtures.isEmpty()) return

    val featured = fixtures.take(3)
    val pagerState = rememberPagerState(pageCount = { featured.size })

    LaunchedEffect(pagerState) {
        while (isActive) {
            delay(5000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % featured.size)
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(228.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                HeroSlide(
                    fixture = featured[page],
                    onWatchClick = onWatchClick,
                    parallaxOffsetPx = parallaxOffsetPx
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(featured.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (pagerState.currentPage == index) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) PitchGreen else SurfaceDark
                        )
                )
            }
        }
    }
}

@Composable
private fun HeroSlide(
    fixture: FixtureItem,
    onWatchClick: (Int) -> Unit,
    parallaxOffsetPx: Float
) {
    val homeGoals = fixture.goals.home ?: 0
    val awayGoals = fixture.goals.away ?: 0
    val live = fixture.isLive()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onWatchClick(fixture.fixture.id) }
    ) {
        AsyncImage(
            model = HERO_STADIUM,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (parallaxOffsetPx * 0.35f).coerceIn(0f, 52f)
                    scaleX = 1.08f
                    scaleY = 1.08f
                }
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CompChip(name = fixture.league.name, leagueId = fixture.league.id)
            if (live) {
                LiveBadge(minute = fixture.fixture.status.elapsed, large = true)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroTeam(fixture.teams.home.name, fixture.teams.home.logo, Alignment.Start)
                Text(
                    text = if (live || fixture.goals.home != null) {
                        "$homeGoals  –  $awayGoals"
                    } else {
                        "VS"
                    },
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = if (live || fixture.goals.home != null) 40.sp else 28.sp,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                HeroTeam(fixture.teams.away.name, fixture.teams.away.logo, Alignment.End)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PitchGreen)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = StadiumBlack, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (live) "WATCH LIVE" else "WATCH",
                    color = StadiumBlack,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun HeroTeam(name: String, logo: String?, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(TextWhite.copy(alpha = 0.12f)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
