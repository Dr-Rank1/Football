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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.model.FixtureEventItem
import com.rank.football.data.model.FixtureStatisticsItem
import com.rank.football.data.model.LineupItem
import com.rank.football.data.model.intValue
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result

private val TAB_LABELS = listOf("Events", "Stats", "Lineups", "Chat")

@Composable
fun MatchDetailTabs(
    events: List<FixtureEventItem>,
    statistics: Result<List<FixtureStatisticsItem>>,
    lineups: Result<List<LineupItem>>,
    currentMinute: Int,
    onEventSeek: (Int) -> Unit,
    fixtureId: Int = 0,
    panelModifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }

    Column(panelModifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TAB_LABELS.forEachIndexed { index, label ->
                val selected = tab == index
                Text(
                    text = label.uppercase(),
                    color = if (selected) StadiumBlack else TextGrey,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) PitchGreen else StadiumBlack.copy(alpha = 0f))
                        .clickable { tab = index }
                        .padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (tab) {
            0 -> {
                MatchTimeline(
                    currentMinute = currentMinute,
                    events = events,
                    onMarkerClick = onEventSeek
                )
                EventsList(events = events)
            }
            1 -> StatsPanel(statistics = statistics)
            2 -> LineupsPanel(lineups = lineups)
            3 -> MatchChatPanel(fixtureId = fixtureId)
        }
    }
}

@Composable
private fun EventsList(events: List<FixtureEventItem>) {
    if (events.isEmpty()) {
        EmptyDetail("No events yet")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        events.takeLast(12).reversed().forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.time.elapsed ?: 0}'",
                    color = PitchGreen,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    modifier = Modifier.width(36.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.type + (event.detail?.let { " · $it" } ?: ""),
                        color = TextWhite,
                        fontFamily = DmSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(event.player?.name, event.team.name).joinToString(" · "),
                        color = TextGrey,
                        fontFamily = DmSans,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsPanel(statistics: Result<List<FixtureStatisticsItem>>) {
    when (statistics) {
        is Result.Loading -> EmptyDetail("Loading stats…")
        is Result.Error -> EmptyDetail(statistics.message)
        is Result.Success -> {
            val home = statistics.data.getOrNull(0)
            val away = statistics.data.getOrNull(1)
            if (home == null || away == null) {
                EmptyDetail("Stats unavailable")
                return
            }
            val types = (home.statistics.map { it.type } + away.statistics.map { it.type })
                .distinct()
                .filter { it.isNotBlank() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(home.team.name, color = TextWhite, fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(away.team.name, color = TextWhite, fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                }
                types.forEach { type ->
                    val hv = home.statistics.firstOrNull { it.type == type }?.intValue() ?: 0
                    val av = away.statistics.firstOrNull { it.type == type }?.intValue() ?: 0
                    val total = (hv + av).coerceAtLeast(1)
                    StatBarRow(label = type, home = hv, away = av, homeFrac = hv.toFloat() / total)
                }
            }
        }
    }
}

@Composable
private fun StatBarRow(label: String, home: Int, away: Int, homeFrac: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$home", color = TextWhite, fontFamily = BarlowCondensed, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(label, color = TextGrey, fontFamily = DmSans, fontSize = 11.sp)
            Text("$away", color = TextWhite, fontFamily = BarlowCondensed, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { homeFrac.coerceIn(0.05f, 0.95f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PitchGreen,
            trackColor = TextWhite.copy(alpha = 0.12f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun LineupsPanel(lineups: Result<List<LineupItem>>) {
    when (lineups) {
        is Result.Loading -> EmptyDetail("Loading lineups…")
        is Result.Error -> EmptyDetail(lineups.message)
        is Result.Success -> {
            if (lineups.data.isEmpty()) {
                EmptyDetail("Lineups unavailable")
                return
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lineups.data.take(2).forEach { lineup ->
                    LineupTeamColumn(
                        lineup = lineup,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LineupTeamColumn(lineup: LineupItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = lineup.team.logo,
                contentDescription = lineup.team.name,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TextGrey.copy(alpha = 0.2f)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    lineup.team.name,
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    lineup.formation ?: "—",
                    color = PitchGreen,
                    fontFamily = DmSans,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("XI", color = TextGrey, fontFamily = BarlowCondensed, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
        (lineup.startXI.orEmpty()).forEach { wrapper ->
            val p = wrapper.player
            Text(
                text = listOfNotNull(p.number?.toString()?.let { "#$it" }, p.name).joinToString(" "),
                color = TextWhite,
                fontFamily = DmSans,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        if (!lineup.substitutes.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("SUBS", color = TextGrey, fontFamily = BarlowCondensed, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
            lineup.substitutes.take(5).forEach { wrapper ->
                val p = wrapper.player
                Text(
                    text = listOfNotNull(p.number?.toString()?.let { "#$it" }, p.name).joinToString(" "),
                    color = TextGrey,
                    fontFamily = DmSans,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDetail(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextGrey, fontFamily = DmSans, fontSize = 13.sp)
    }
}
