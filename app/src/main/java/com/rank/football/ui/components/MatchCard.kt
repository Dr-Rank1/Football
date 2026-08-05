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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.kickOffTime
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
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
    val homeGoals = fixture.goals.home
    val awayGoals = fixture.goals.away
    val scoreKey = "$homeGoals-$awayGoals"
    var previousScore by remember(fixture.fixture.id) { mutableStateOf(scoreKey) }
    var lastGoalAt by remember(fixture.fixture.id) { mutableLongStateOf(0L) }
    val scoreScale = remember { Animatable(1f) }

    LaunchedEffect(scoreKey) {
        if (previousScore != scoreKey && isLive) {
            lastGoalAt = System.currentTimeMillis()
            scoreScale.snapTo(1.3f)
            scoreScale.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        previousScore = scoreKey
    }

    val recentGoal = isLive && lastGoalAt > 0L && System.currentTimeMillis() - lastGoalAt < 120_000L

    val cardShape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(cardShape)
            .background(CardDark)
            .border(
                width = 3.dp,
                color = if (isLive) LiveRed else Color.Transparent,
                shape = cardShape
            )
            .border(
                width = 1.dp,
                color = if (recentGoal) PitchGreen else TextWhite.copy(alpha = if (isLive) 0.1f else 0.055f),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fixture.league.name,
                color = TextGrey,
                fontFamily = DmSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            StatusChip(
                status = fixture.fixture.status.short,
                elapsed = fixture.fixture.status.elapsed
            )
        }

        Spacer(Modifier.height(9.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamCrest(
                    name = fixture.teams.home.name,
                    logo = fixture.teams.home.logo,
                    size = 28.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = lastWord(fixture.teams.home.name),
                    color = TextWhite,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (homeGoals != null && awayGoals != null) {
                    Text(
                        text = "$homeGoals – $awayGoals",
                        color = TextWhite,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = (-1).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scoreScale.value
                            scaleY = scoreScale.value
                        }
                    )
                } else {
                    Text(
                        text = fixture.kickOffTime(),
                        color = GoalYellow,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastWord(fixture.teams.away.name),
                    color = TextWhite,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.width(8.dp))
                TeamCrest(
                    name = fixture.teams.away.name,
                    logo = fixture.teams.away.logo,
                    size = 28.dp
                )
            }
        }
    }
}

private fun lastWord(name: String): String {
    val trimmed = name.trim()
    val parts = trimmed.split(Regex("\\s+"))
    return if (parts.size > 1) parts.last() else trimmed
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
