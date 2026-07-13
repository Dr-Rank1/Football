package com.rank.football.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.model.kickOffTime
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
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
    if (compact) {
        CompactOnNowCard(fixture = fixture, onClick = onClick, modifier = Modifier)
    } else {
        FullMatchCard(
            fixture = fixture,
            onClick = onClick,
            modifier = Modifier,
            favoritesRepository = favoritesRepository
        )
    }
}

@Composable
private fun CompactOnNowCard(
    fixture: FixtureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = fixture.isLive()
    val homeGoals = fixture.goals.home ?: 0
    val awayGoals = fixture.goals.away ?: 0
    val scoreKey = "$homeGoals-$awayGoals"
    var previousScore by remember(fixture.fixture.id) { mutableStateOf(scoreKey) }
    var lastGoalAt by remember(fixture.fixture.id) { mutableLongStateOf(0L) }

    LaunchedEffect(scoreKey) {
        if (previousScore != scoreKey && isLive) lastGoalAt = System.currentTimeMillis()
        previousScore = scoreKey
    }
    val recentGoal = isLive && lastGoalAt > 0L && System.currentTimeMillis() - lastGoalAt < 120_000L
    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)

    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (recentGoal) PitchGreen else TextWhite.copy(alpha = 0.06f),
                RoundedCornerShape(16.dp)
            )
            .background(CardDark)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (recentGoal) PitchGreen else accent)
        )
        Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(if (isLive) LiveRed else accent, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isLive) "${fixture.fixture.status.elapsed ?: 0}'" else fixture.kickOffTime(),
                color = LiveRed,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
            Text(" Â· ", color = TextWhite.copy(alpha = 0.25f), fontSize = 8.sp)
            Text(
                text = fixture.league.name,
                color = TextWhite.copy(alpha = 0.35f),
                fontFamily = DmSans,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (recentGoal) {
                Text(
                    "GOAL!",
                    color = GoalYellow,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        ScoreLine(name = fixture.teams.home.name, logo = fixture.teams.home.logo, score = homeGoals, flash = recentGoal)
        Spacer(Modifier.height(6.dp))
        ScoreLine(name = fixture.teams.away.name, logo = fixture.teams.away.logo, score = awayGoals, flash = false)
        }
    }
}

@Composable
private fun ScoreLine(name: String, logo: String?, score: Int, flash: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TextGrey.copy(alpha = 0.2f)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = name,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$score",
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FullMatchCard(
    fixture: FixtureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favoritesRepository: FavoritesRepository? = null
) {
    val isLive = fixture.isLive()
    var countdown by remember(fixture.fixture.id) { mutableStateOf<String?>(null) }
    val homeGoals = fixture.goals.home ?: 0
    val awayGoals = fixture.goals.away ?: 0
    val scoreKey = "$homeGoals-$awayGoals"
    var previousScore by remember(fixture.fixture.id) { mutableStateOf(scoreKey) }
    var lastGoalAt by remember(fixture.fixture.id) { mutableLongStateOf(0L) }
    val scoreScale = remember { Animatable(1f) }

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
            scoreScale.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        previousScore = scoreKey
    }

    val recentGoal = isLive && lastGoalAt > 0L && System.currentTimeMillis() - lastGoalAt < 120_000L
    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)
    val leftAccent = if (recentGoal) PitchGreen else accent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (recentGoal) PitchGreen else TextWhite.copy(alpha = 0.06f),
                RoundedCornerShape(16.dp)
            )
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(leftAccent.copy(alpha = 0.16f), CardDark, CardDark)
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row {
            Box(
                Modifier
                    .width(3.dp)
                    .height(if (isLive) 140.dp else 88.dp)
                    .background(leftAccent)
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recentGoal) {
                        val pulse = rememberInfiniteTransition(label = "goal_chip")
                        val a by pulse.animateFloat(0.65f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), "ga")
                        Text(
                            text = "GOAL!",
                            color = StadiumBlack,
                            fontFamily = DmSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .graphicsLayer { alpha = a }
                                .background(GoalYellow, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    } else if (isLive) {
                        LiveBadge(minute = fixture.fixture.status.elapsed, large = true)
                    } else {
                        Text(
                            text = countdown ?: fixture.kickOffTime(),
                            color = PitchGreen,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    CompChip(name = fixture.league.name, leagueId = fixture.league.id, compact = true)
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamBlock(
                        name = fixture.teams.home.name,
                        logo = fixture.teams.home.logo,
                        teamId = fixture.teams.home.id,
                        leagueId = fixture.league.id,
                        leagueName = fixture.league.name,
                        alignEnd = false,
                        favoritesRepository = favoritesRepository,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (isLive || fixture.goals.home != null) {
                            Text(
                                text = "$homeGoals",
                                color = TextWhite,
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scoreScale.value
                                    scaleY = scoreScale.value
                                }
                            )
                            Text("â€“", color = TextWhite.copy(alpha = 0.25f), fontSize = 14.sp)
                            Text(
                                text = "$awayGoals",
                                color = TextWhite,
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            )
                        } else {
                            Text(
                                "VS",
                                color = TextWhite.copy(alpha = 0.3f),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                    TeamBlock(
                        name = fixture.teams.away.name,
                        logo = fixture.teams.away.logo,
                        teamId = fixture.teams.away.id,
                        leagueId = fixture.league.id,
                        leagueName = fixture.league.name,
                        alignEnd = true,
                        favoritesRepository = favoritesRepository,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isLive) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PitchGreen)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = StadiumBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "WATCH NOW",
                            color = StadiumBlack,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamBlock(
    name: String,
    logo: String?,
    teamId: Int?,
    leagueId: Int,
    leagueName: String,
    alignEnd: Boolean,
    favoritesRepository: FavoritesRepository?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        if (!alignEnd) {
            Crest(logo, name)
            Spacer(Modifier.width(6.dp))
            if (favoritesRepository != null) {
                FavoriteButton(
                    teamId = teamId,
                    teamName = name,
                    teamLogo = logo,
                    leagueId = leagueId,
                    leagueName = leagueName,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
        }
        Text(
            text = name,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (alignEnd) {
            if (favoritesRepository != null) {
                Spacer(Modifier.width(4.dp))
                FavoriteButton(
                    teamId = teamId,
                    teamName = name,
                    teamLogo = logo,
                    leagueId = leagueId,
                    leagueName = leagueName,
                    favoritesRepository = favoritesRepository,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Crest(logo, name)
        }
    }
}

@Composable
private fun Crest(logo: String?, name: String) {
    AsyncImage(
        model = logo,
        contentDescription = name,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(TextGrey.copy(alpha = 0.2f)),
        contentScale = ContentScale.Fit
    )
}
