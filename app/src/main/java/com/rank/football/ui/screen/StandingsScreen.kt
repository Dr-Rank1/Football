package com.rank.football.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.CachedStanding
import com.rank.football.ui.components.DetailBackHeader
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/** League standings table with position color coding. */
@Composable
fun StandingsScreen(leagueId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).standingsDao() }
    val rows by dao.byLeague(leagueId).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
    ) {
        item {
            DetailBackHeader(
                title = stringResource(R.string.standings_title),
                onBack = onBack
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("#", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp))
                Text(
                    stringResource(R.string.standings_club),
                    color = TextGrey,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                Text("P", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp))
                Text("GD", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                Text("Pts", color = TextGrey, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp))
            }
        }
        items(rows, key = { it.teamId }) { row ->
            StandingRow(row, rows.size)
        }
    }
}

@Composable
private fun StandingRow(row: CachedStanding, totalTeams: Int) {
    val accent = when {
        row.position <= 4 -> PitchGreen
        row.position == 5 -> GoalYellow
        row.position > totalTeams - 3 -> LiveRed
        else -> SurfaceDark
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = if (accent == SurfaceDark) 1f else 0.35f), RoundedCornerShape(12.dp))
            .background(if (accent == SurfaceDark) CardDark else accent.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            "${row.position}",
            color = TextWhite,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(28.dp)
        )
        Text(
            row.teamName,
            color = TextWhite,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text("${row.played}", color = TextGrey, modifier = Modifier.width(28.dp))
        Text("${row.goalsFor - row.goalsAgainst}", color = TextGrey, modifier = Modifier.width(32.dp))
        Text(
            "${row.points}",
            color = PitchGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
    }
}
