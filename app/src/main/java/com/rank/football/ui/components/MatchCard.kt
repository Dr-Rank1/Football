package com.rank.football.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.model.kickOffTime
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.accessibility.scoreContentDescription
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.MatchCountdown
import kotlinx.coroutines.delay

@Composable
fun MatchCard(
    fixture: FixtureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favoritesRepository: FavoritesRepository? = null,
    compact: Boolean = false
) {
    val isLive = fixture.isLive()
    var countdown by remember(fixture.fixture.id) { mutableStateOf<String?>(null) }
    val homeGoals = fixture.goals.home ?: 0
    val awayGoals = fixture.goals.away ?: 0
    val scoreKey = "$homeGoals-$awayGoals"
    var previousScore by remember(fixture.fixture.id) { mutableStateOf(scoreKey) }
    var lastGoalAt by remember(fixture.fixture.id) { mutableLongStateOf(0L) }
    val scoreScale = remember { Animatable(1f) }
    val ringAlpha = remember { Animatable(0f) }

    LaunchedEffect(fixture.fixture.id, fixture.fixture.date) {
        if (fixture.isUpcoming()) {
            while (true) {
                countdown = MatchCountdown.countdownText(fixture)
                delay(60_000)
            }
        }
    }

    LaunchedEffect(scoreKey) {
        if (previousScore != scoreKey && isLive) {
            lastGoalAt = System.currentTimeMillis()
            scoreScale.snapTo(1.3f)
            ringAlpha.snapTo(0.7f)
            scoreScale.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
            ringAlpha.animateTo(0f, tween(1600))
        }
        previousScore = scoreKey
    }

    val recentGoal = isLive && lastGoalAt > 0L &&
        System.currentTimeMillis() - lastGoalAt < 120_000L

    // Keep recomposing while GOAL window is active
    if (recentGoal) {
        LaunchedEffect(lastGoalAt) {
            delay(120_000)
        }
    }

    val liveDesc = if (isLive) {
        stringResource(R.string.live_minute, fixture.fixture.status.elapsed ?: 0)
    } else if (countdown != null) {
        countdown!!
    } else {
        fixture.kickOffTime()
    }
    val cardDescription = "${fixture.teams.home.name} versus ${fixture.teams.away.name}, " +
        "${scoreContentDescription(homeGoals, awayGoals)}, $liveDesc, ${fixture.league.name}"

    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)
    val borderColor = when {
        recentGoal -> PitchGreen.copy(alpha = 0.85f)
        isLive -> LiveRed.copy(alpha = 0.5f)
        else -> accent.copy(alpha = 0.55f)
    }

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(if (isLive || recentGoal) 6.dp else 2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (recentGoal) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(CardDark)
                .semantics(mergeDescendants = true) { contentDescription = cardDescription }
                .clickable(onClick = onClick)
                .padding(start = 3.dp)
                .background(CardDark)
                .padding(if (compact) 10.dp else 14.dp)
        ) {
            // Competition accent bar via start padding strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left accent
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(10.dp))

                TeamColumn(
                    name = fixture.teams.home.name,
                    logo = fixture.teams.home.logo,
                    teamId = fixture.teams.home.id,
                    leagueId = fixture.league.id,
                    leagueName = fixture.league.name,
                    alignment = Alignment.Start,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    val scoreText = if (isLive || fixture.goals.home != null) fixture.displayScore() else "vs"
                    AnimatedContent(
                        targetState = scoreText,
                        transitionSpec = {
                            slideInVertically { it } togetherWith slideOutVertically { -it }
                        },
                        label = "score_anim"
                    ) { score ->
                        Text(
                            text = score,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLive) NeonGreen else TextWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scoreScale.value
                                scaleY = scoreScale.value
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    when {
                        recentGoal -> {
                            val pulse = rememberInfiniteTransition(label = "goal_badge")
                            val alpha by pulse.animateFloat(
                                initialValue = 0.55f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    tween(500),
                                    RepeatMode.Reverse
                                ),
                                label = "goal_alpha"
                            )
                            Text(
                                text = "GOAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoalYellow.copy(alpha = alpha),
                                fontWeight = FontWeight.Black
                            )
                        }
                        isLive -> {
                            Text(
                                text = stringResource(R.string.live_minute, fixture.fixture.status.elapsed ?: 0),
                                style = MaterialTheme.typography.labelSmall,
                                color = LiveRed
                            )
                        }
                        countdown != null -> {
                            Text(text = countdown!!, style = MaterialTheme.typography.labelSmall, color = PitchGreen)
                        }
                        else -> {
                            Text(
                                text = fixture.kickOffTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrey
                            )
                        }
                    }
                }

                TeamColumn(
                    name = fixture.teams.away.name,
                    logo = fixture.teams.away.logo,
                    teamId = fixture.teams.away.id,
                    leagueId = fixture.league.id,
                    leagueName = fixture.league.name,
                    alignment = Alignment.End,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fixture.league.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextGrey,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Glow ring on goal
        if (ringAlpha.value > 0.02f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = ringAlpha.value }
                    .border(2.dp, PitchGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
private fun TeamColumn(
    name: String,
    logo: String?,
    teamId: Int?,
    leagueId: Int,
    leagueName: String,
    alignment: Alignment.Horizontal,
    favoritesRepository: FavoritesRepository?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (alignment == Alignment.End && favoritesRepository != null) {
                FavoriteButton(
                    teamId = teamId,
                    teamName = name,
                    teamLogo = logo,
                    leagueId = leagueId,
                    leagueName = leagueName,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            AsyncImage(
                model = logo,
                contentDescription = name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TextGrey.copy(alpha = 0.2f)),
                contentScale = ContentScale.Fit
            )
            if (alignment == Alignment.Start && favoritesRepository != null) {
                Spacer(modifier = Modifier.width(4.dp))
                FavoriteButton(
                    teamId = teamId,
                    teamName = name,
                    teamLogo = logo,
                    leagueId = leagueId,
                    leagueName = leagueName,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhite,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignment == Alignment.Start) TextAlign.Start else TextAlign.End
        )
    }
}
