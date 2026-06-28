package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/** Screen context for tailoring the no-streams empty state copy. */
enum class NoStreamsContext {
    HOME, LIVE, FIXTURES, LEAGUES
}

/** Rich empty state shown when no streamable matches are available. */
@Composable
fun NoStreamsEmptyState(
    context: NoStreamsContext,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onBrowseFixtures: (() -> Unit)? = null,
    onBrowseLeagues: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val bodyRes = when (context) {
        NoStreamsContext.HOME -> R.string.empty_streams_home_body
        NoStreamsContext.LIVE -> R.string.empty_streams_live_body
        NoStreamsContext.FIXTURES -> R.string.empty_streams_fixtures_body
        NoStreamsContext.LEAGUES -> R.string.empty_streams_leagues_body
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = if (compact) 4.dp else 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceDark, RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PitchGreen.copy(alpha = 0.08f),
                        CardDark,
                        CardDark
                    )
                )
            )
            .padding(if (compact) 20.dp else 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact) {
            PitchIllustration()
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = stringResource(R.string.empty_streams_title),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )

        if (!compact) {
            Spacer(modifier = Modifier.height(16.dp))
            EmptyStateTips()
            Spacer(modifier = Modifier.height(20.dp))

            if (onBrowseFixtures != null || onBrowseLeagues != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onBrowseFixtures != null) {
                        OutlinedButton(
                            onClick = onBrowseFixtures,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = PitchGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.empty_streams_browse_fixtures),
                                color = TextWhite,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    if (onBrowseLeagues != null) {
                        OutlinedButton(
                            onClick = onBrowseLeagues,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = PitchGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.empty_streams_browse_leagues),
                                color = TextWhite,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (onRefresh != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PitchGreen.copy(alpha = 0.15f),
                        contentColor = NeonGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.empty_streams_refresh))
                }
            }
        }
    }
}

@Composable
private fun PitchIllustration() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PitchGreen.copy(alpha = 0.12f))
            .border(1.dp, PitchGreen.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(2.dp, PitchGreen.copy(alpha = 0.4f), CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(PitchGreen.copy(alpha = 0.25f))
        )
        Text(
            text = "⏸",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .background(StadiumBlack.copy(alpha = 0.5f), CircleShape)
                .padding(8.dp)
        )
    }
}

@Composable
private fun EmptyStateTips() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StadiumBlack.copy(alpha = 0.4f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipRow(stringResource(R.string.empty_streams_tip_fixtures))
        TipRow(stringResource(R.string.empty_streams_tip_leagues))
        TipRow(stringResource(R.string.empty_streams_tip_notify))
    }
}

@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", color = PitchGreen, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = TextGrey, style = MaterialTheme.typography.bodySmall)
    }
}
