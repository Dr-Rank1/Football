package com.rank.football.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.R
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.FavoriteTeam
import com.rank.football.data.local.OnboardingPreference
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.launch

private data class OnboardTeam(
    val id: Int,
    val name: String,
    val leagueId: Int,
    val leagueName: String,
    val logo: String = "https://media.api-sports.io/football/teams/$id.png"
)

private data class OnboardLeague(
    val id: Int,
    val name: String,
    val logo: String = "https://media.api-sports.io/football/leagues/$id.png"
)

private val POPULAR_TEAMS = listOf(
    OnboardTeam(50, "Man City", 39, "Premier League"),
    OnboardTeam(42, "Arsenal", 39, "Premier League"),
    OnboardTeam(40, "Liverpool", 39, "Premier League"),
    OnboardTeam(33, "Man United", 39, "Premier League"),
    OnboardTeam(49, "Chelsea", 39, "Premier League"),
    OnboardTeam(47, "Tottenham", 39, "Premier League"),
    OnboardTeam(541, "Real Madrid", 140, "La Liga"),
    OnboardTeam(529, "Barcelona", 140, "La Liga"),
    OnboardTeam(530, "Atletico Madrid", 140, "La Liga"),
    OnboardTeam(157, "Bayern", 78, "Bundesliga"),
    OnboardTeam(165, "Dortmund", 78, "Bundesliga"),
    OnboardTeam(85, "PSG", 61, "Ligue 1"),
    OnboardTeam(489, "AC Milan", 135, "Serie A"),
    OnboardTeam(505, "Inter", 135, "Serie A"),
    OnboardTeam(496, "Juventus", 135, "Serie A")
)

private val POPULAR_LEAGUES = listOf(
    OnboardLeague(39, "Premier League"),
    OnboardLeague(140, "La Liga"),
    OnboardLeague(78, "Bundesliga"),
    OnboardLeague(135, "Serie A"),
    OnboardLeague(61, "Ligue 1"),
    OnboardLeague(2, "Champions League"),
    OnboardLeague(3, "Europa League"),
    OnboardLeague(1, "World Cup")
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val favoritesRepo = remember { FavoritesRepository(AppDatabase.getInstance(context)) }
    var step by remember { mutableIntStateOf(0) }
    var selectedTeams by remember { mutableStateOf(setOf<Int>()) }
    var selectedLeagues by remember { mutableStateOf(setOf<Int>()) }

    fun finish() {
        scope.launch {
            selectedTeams.forEach { id ->
                val t = POPULAR_TEAMS.firstOrNull { it.id == id } ?: return@forEach
                favoritesRepo.addFavorite(
                    FavoriteTeam(
                        teamId = t.id,
                        teamName = t.name,
                        teamLogo = t.logo,
                        leagueId = t.leagueId,
                        leagueName = t.leagueName
                    )
                )
            }
            OnboardingPreference.setPreferredLeagueIds(context, selectedLeagues)
            OnboardingPreference.setOnboardingComplete(context)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
    ) {
        TextButton(
            onClick = { finish() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.skip), color = TextGrey)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (step) {
                    0 -> stringResource(R.string.onboarding_title)
                    1 -> "Pick your clubs"
                    else -> "Follow leagues"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (step) {
                    0 -> stringResource(R.string.onboarding_body)
                    1 -> "Select at least 3 favourites for a personalised Home."
                    else -> "Choose competitions you care about."
                },
                color = TextGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
            )

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (i == step) PitchGreen else SurfaceDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                0 -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = StadiumBlack)
                    ) {
                        Text(stringResource(R.string.get_started), fontWeight = FontWeight.Bold)
                    }
                }
                1 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(POPULAR_TEAMS, key = { it.id }) { team ->
                            val selected = team.id in selectedTeams
                            val scale by animateFloatAsState(if (selected) 1.06f else 1f, label = "crest")
                            val border by animateColorAsState(
                                if (selected) PitchGreen else TextGrey.copy(alpha = 0.2f),
                                label = "border"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .scale(scale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, border, RoundedCornerShape(14.dp))
                                    .background(CardDark)
                                    .clickable {
                                        selectedTeams = if (selected) selectedTeams - team.id
                                        else selectedTeams + team.id
                                    }
                                    .padding(10.dp)
                            ) {
                                AsyncImage(
                                    model = team.logo,
                                    contentDescription = team.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(TextGrey.copy(alpha = 0.15f)),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    team.name,
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { if (selectedTeams.size >= 3) step = 2 },
                        enabled = selectedTeams.size >= 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchGreen,
                            contentColor = StadiumBlack,
                            disabledContainerColor = SurfaceDark,
                            disabledContentColor = TextGrey
                        )
                    ) {
                        Text("Continue (${selectedTeams.size}/3+)", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(POPULAR_LEAGUES, key = { it.id }) { league ->
                            val selected = league.id in selectedLeagues
                            val border by animateColorAsState(
                                if (selected) PitchGreen else TextGrey.copy(alpha = 0.2f),
                                label = "lborder"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, border, RoundedCornerShape(14.dp))
                                    .background(CardDark)
                                    .clickable {
                                        selectedLeagues = if (selected) selectedLeagues - league.id
                                        else selectedLeagues + league.id
                                    }
                                    .padding(12.dp)
                            ) {
                                AsyncImage(
                                    model = league.logo,
                                    contentDescription = league.name,
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    league.name,
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = StadiumBlack)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
