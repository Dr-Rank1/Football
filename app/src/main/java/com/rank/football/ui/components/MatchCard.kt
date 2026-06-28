package com.rank.football.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.model.kickOffTime
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.MatchCountdown
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rank.football.ui.accessibility.scoreContentDescription
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

    LaunchedEffect(fixture.fixture.id, fixture.fixture.date) {
        if (fixture.isUpcoming()) {
            while (true) {
                countdown = MatchCountdown.countdownText(fixture)
                delay(60_000)
            }
        }
    }

    val homeGoals = fixture.goals.home ?: 0
    val awayGoals = fixture.goals.away ?: 0
    val liveDesc = if (isLive) {
        stringResource(R.string.live_minute, fixture.fixture.status.elapsed ?: 0)
    } else if (countdown != null) {
        countdown!!
    } else {
        fixture.kickOffTime()
    }
    val cardDescription = "${fixture.teams.home.name} versus ${fixture.teams.away.name}, " +
        "${scoreContentDescription(homeGoals, awayGoals)}, $liveDesc, ${fixture.league.name}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .shadow(if (isLive) 4.dp else 2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isLive) Modifier.border(1.dp, LiveRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .background(CardDark)
            .semantics(mergeDescendants = true) { contentDescription = cardDescription }
            .clickable(onClick = onClick)
            .padding(if (compact) 10.dp else 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                when {
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
