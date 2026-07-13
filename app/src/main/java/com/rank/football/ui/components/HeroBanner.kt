package com.rank.football.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.R
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.displayScore
import com.rank.football.data.model.isLive
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/**
 * Full-bleed Home match stage: brand-first, one featured fixture, one CTA.
 * No inset card, no pager dots, no floating chips on the media.
 */
@Composable
fun HeroBanner(
    fixtures: List<FixtureItem>,
    onWatchClick: (Int) -> Unit,
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    stageModifier: Modifier = Modifier,
    parallaxOffsetPx: Float = 0f
) {
    val fixture = fixtures.firstOrNull()
    Box(
        modifier = stageModifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        if (fixture == null) {
            BrandOnlyStage(
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                parallaxOffsetPx = parallaxOffsetPx
            )
        } else {
            MatchStage(
                fixture = fixture,
                onWatchClick = onWatchClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
                parallaxOffsetPx = parallaxOffsetPx
            )
        }
    }
}

@Composable
private fun MatchStage(
    fixture: FixtureItem,
    onWatchClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    parallaxOffsetPx: Float
) {
    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)
    val live = fixture.isLive()
    val drift by rememberInfiniteTransition(label = "crestDrift").animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crestDriftAnim"
    )

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
                        colors = listOf(
                            accent.copy(alpha = 0.55f),
                            StadiumBlack.copy(alpha = 0.85f),
                            StadiumBlack
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )
        AsyncImage(
            model = fixture.teams.home.logo,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    alpha = 0.12f
                    translationX = -24f + drift
                    translationY = parallaxOffsetPx * 0.2f
                    scaleX = 1.35f
                    scaleY = 1.35f
                }
        )
        AsyncImage(
            model = fixture.teams.away.logo,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    alpha = 0.12f
                    translationX = 24f - drift
                    translationY = parallaxOffsetPx * 0.15f
                    scaleX = 1.35f
                    scaleY = 1.35f
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            StadiumBlack.copy(alpha = 0.35f),
                            StadiumBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            StageTopBar(onSearchClick = onSearchClick, onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                letterSpacing = (-0.5).sp,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (live) {
                    stringResource(R.string.hero_live_line, fixture.teams.home.name, fixture.teams.away.name)
                } else {
                    stringResource(R.string.hero_watch_line, fixture.teams.home.name, fixture.teams.away.name)
                },
                color = TextWhite.copy(alpha = 0.82f),
                fontFamily = DmSans,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StageCrest(fixture.teams.home.logo, fixture.teams.home.name)
                Text(
                    text = if (live || fixture.goals.home != null) fixture.displayScore().replace("-", "–") else "VS",
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    letterSpacing = (-1).sp
                )
                StageCrest(fixture.teams.away.logo, fixture.teams.away.name)
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PitchGreen)
                    .clickable { onWatchClick(fixture.fixture.id) }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = StadiumBlack, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (live) stringResource(R.string.hero_cta_live) else stringResource(R.string.hero_cta_watch),
                    color = StadiumBlack,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun BrandOnlyStage(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    parallaxOffsetPx: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PitchGreen.copy(alpha = 0.28f),
                        StadiumBlack
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = parallaxOffsetPx * 0.25f }
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonGreen.copy(alpha = 0.16f), Color.Transparent),
                        radius = 700f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            StageTopBar(onSearchClick = onSearchClick, onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                letterSpacing = (-0.5).sp,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.hero_empty_support),
                color = TextGrey,
                fontFamily = DmSans,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun StageTopBar(onSearchClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = TextWhite.copy(alpha = 0.75f))
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = TextWhite.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun StageCrest(logo: String?, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(88.dp)) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(TextWhite.copy(alpha = 0.1f))
                .padding(6.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
