package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rank.football.R
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isFinished
import com.rank.football.data.repository.FootballRepository
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.MatchReminderScheduler
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.Year
import java.time.format.DateTimeParseException

/** Bottom sheet for upcoming fixtures: countdown, H2H, form, and reminder. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMatchSheet(
    fixture: FixtureItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { FootballRepository(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var remainingMs by remember(fixture.fixture.id) { mutableLongStateOf(sheetKickoffLeft(fixture)) }
    var h2h by remember { mutableStateOf<HypeH2H?>(null) }
    var homeForm by remember { mutableStateOf("") }
    var awayForm by remember { mutableStateOf("") }
    var reminderSet by remember { mutableStateOf(false) }

    LaunchedEffect(fixture.fixture.id, fixture.fixture.date) {
        while (true) {
            remainingMs = sheetKickoffLeft(fixture)
            if (remainingMs <= 0L) break
            delay(1000)
        }
    }

    LaunchedEffect(fixture.fixture.id) {
        val homeId = fixture.teams.home.id ?: return@LaunchedEffect
        val awayId = fixture.teams.away.id ?: return@LaunchedEffect
        val matches = repo.getHeadToHead(homeId, awayId)
        var hw = 0; var d = 0; var aw = 0
        matches.filter { it.isFinished() }.forEach { m ->
            val hg = m.goals.home ?: return@forEach
            val ag = m.goals.away ?: return@forEach
            when {
                hg > ag && m.teams.home.id == homeId -> hw++
                ag > hg && m.teams.away.id == homeId -> hw++
                hg > ag && m.teams.home.id == awayId -> aw++
                ag > hg && m.teams.away.id == awayId -> aw++
                hg == ag -> d++
            }
        }
        h2h = HypeH2H(hw, d, aw)
        val season = fixture.league.season ?: Year.now().value
        homeForm = repo.getTeamForm(homeId, fixture.league.id, season).takeIf { it != "N/A" }.orEmpty()
        awayForm = repo.getTeamForm(awayId, fixture.league.id, season).takeIf { it != "N/A" }.orEmpty()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardDark
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = PitchGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = fixture.league.name,
                    color = TextGrey,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = formatSheetCountdown(remainingMs),
                color = NeonGreen,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.upcoming_kickoff_in),
                color = TextGrey,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SheetTeam(name = fixture.teams.home.name, logo = fixture.teams.home.logo, form = homeForm)
                Text("VS", color = TextWhite, fontWeight = FontWeight.Bold)
                SheetTeam(name = fixture.teams.away.name, logo = fixture.teams.away.logo, form = awayForm)
            }
            h2h?.let { record ->
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.upcoming_h2h),
                    color = TextGrey,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.upcoming_h2h_record,
                        record.homeWins,
                        record.draws,
                        record.awayWins
                    ),
                    color = TextWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    MatchReminderScheduler.scheduleReminder(
                        context = context,
                        fixtureId = fixture.fixture.id,
                        homeTeam = fixture.teams.home.name,
                        awayTeam = fixture.teams.away.name,
                        kickOffIso = fixture.fixture.date
                    )
                    reminderSet = true
                },
                enabled = !reminderSet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PitchGreen.copy(alpha = 0.2f),
                    contentColor = NeonGreen,
                    disabledContainerColor = SurfaceDark,
                    disabledContentColor = TextGrey
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    if (reminderSet) stringResource(R.string.upcoming_reminder_set)
                    else stringResource(R.string.set_reminder)
                )
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGrey)
            }
        }
    }
}

@Composable
private fun SheetTeam(name: String, logo: String?, form: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(StadiumBlack),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(8.dp))
        Text(name, color = TextWhite, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        if (form.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(form.takeLast(5), color = PitchGreen, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun sheetKickoffLeft(fixture: FixtureItem): Long {
    val raw = fixture.fixture.date
    val instant = try {
        Instant.parse(raw)
    } catch (_: DateTimeParseException) {
        return 0L
    }
    return (instant.toEpochMilli() - System.currentTimeMillis()).coerceAtLeast(0L)
}

private fun formatSheetCountdown(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}