package com.rank.football.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.rank.football.R
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.kickOffTime
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    fixtures: List<FixtureItem>,
    onWatchClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (fixtures.isEmpty()) return

    val featured = fixtures.take(3)
    val pagerState = rememberPagerState(pageCount = { featured.size })

    LaunchedEffect(pagerState) {
        while (isActive) {
            delay(5000)
            val next = (pagerState.currentPage + 1) % featured.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                Crossfade(targetState = featured[page], label = "hero") { fixture ->
                    HeroBannerItem(fixture = fixture, onWatchClick = onWatchClick)
                }
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
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
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
private fun HeroBannerItem(
    fixture: FixtureItem,
    onWatchClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var gradientStart by remember(fixture.fixture.id) { mutableStateOf(SurfaceDark) }

    LaunchedEffect(fixture.teams.home.logo) {
        try {
            val request = ImageRequest.Builder(context)
                .data(fixture.teams.home.logo)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            val bitmap = result.drawable?.toBitmap()
            if (bitmap != null) {
                val palette = Palette.from(bitmap).generate()
                val swatch = palette.vibrantSwatch ?: palette.dominantSwatch
                if (swatch != null) {
                    gradientStart = Color(swatch.rgb).copy(alpha = 0.6f)
                }
            }
        } catch (_: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onWatchClick(fixture.fixture.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(gradientStart, StadiumBlack)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = fixture.league.logo,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = fixture.league.name,
                    color = TextWhite,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = "${fixture.teams.home.name}  vs  ${fixture.teams.away.name}",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fixture.isLive()) {
                    Text(
                        text = stringResource(R.string.live_minute, fixture.fixture.status.elapsed ?: 0),
                        color = LiveRed,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = fixture.kickOffTime(),
                        color = TextWhite
                    )
                }
                Text(
                    text = stringResource(R.string.hero_watch),
                    color = StadiumBlack,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchGreen)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
