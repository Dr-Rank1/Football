package com.rank.football.ui.components

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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.format.DateTimeParseException

data class HypeH2H(val homeWins: Int, val draws: Int, val awayWins: Int)

@Composable
fun PreMatchHypeCard(
    fixture: FixtureItem,
    h2h: HypeH2H?,
    homeForm: String,
    awayForm: String,
    onClick: () -> Unit,
    cardModifier: Modifier = Modifier
) {
    if (fixture.isLive()) return

    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)
    var remainingMs by remember(fixture.fixture.id) { mutableLongStateOf(kickoffMillisLeft(fixture)) }

    LaunchedEffect(fixture.fixture.id, fixture.fixture.date) {
        while (true) {
            remainingMs = kickoffMillisLeft(fixture)
            if (remainingMs <= 0L) break
            delay(1000)
        }
    }

    if (remainingMs > 90 * 60 * 1000L) return

    val total = ((h2h?.homeWins ?: 0) + (h2h?.draws ?: 0) + (h2h?.awayWins ?: 0)).coerceAtLeast(1)
    val hwPct = ((h2h?.homeWins ?: 0) * 100f / total).coerceAtLeast(8f)
    val awPct = ((h2h?.awayWins ?: 0) * 100f / total).coerceAtLeast(8f)

    Column(cardModifier.padding(horizontal = 20.dp)) {
        Text(
            text = "COMING UP",
            color = TextGrey.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, TextWhite.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                .background(CardDark)
                .clickable(onClick = onClick)
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(140.dp)
                    .background(accent)
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fixture.league.name,
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = TextGrey, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatCountdown(remainingMs),
                            color = PitchGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamCrestBlock(name = fixture.teams.home.name, logo = fixture.teams.home.logo, form = homeForm)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HEAD TO HEAD",
                            color = TextGrey.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth().height(6.dp)) {
                            Box(
                                Modifier
                                    .weight(hwPct)
                                    .height(6.dp)
                                    .background(PitchGreen, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                            )
                            Box(
                                Modifier
                                    .width(8.dp)
                                    .height(6.dp)
                                    .background(TextWhite.copy(alpha = 0.2f))
                            )
                            Box(
                                Modifier
                                    .weight(awPct)
                                    .height(6.dp)
                                    .background(LiveRed, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${h2h?.homeWins ?: 0}W", color = PitchGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${h2h?.draws ?: 0}D", color = TextGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${h2h?.awayWins ?: 0}W", color = LiveRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    TeamCrestBlock(name = fixture.teams.away.name, logo = fixture.teams.away.logo, form = awayForm)
                }
            }
        }
    }
}

@Composable
private fun TeamCrestBlock(name: String, logo: String?, form: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TextGrey.copy(alpha = 0.15f)),
            contentScale = ContentScale.Fit
        )
        Text(
            text = name.split(" ").take(2).joinToString(" "),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (form.isNotBlank()) {
            FormDots(form = form.takeLast(5))
        }
    }
}

@Composable
private fun FormDots(form: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        form.forEach { ch ->
            val color = when (ch.uppercaseChar()) {
                'W' -> PitchGreen
                'D' -> TextGrey
                'L' -> LiveRed
                else -> TextGrey.copy(alpha = 0.3f)
            }
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
    }
}

private fun kickoffMillisLeft(fixture: FixtureItem): Long {
    return try {
        val kick = Instant.parse(fixture.fixture.date).toEpochMilli()
        (kick - System.currentTimeMillis()).coerceAtLeast(0L)
    } catch (_: DateTimeParseException) {
        Long.MAX_VALUE
    } catch (_: Exception) {
        Long.MAX_VALUE
    }
}

private fun formatCountdown(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
