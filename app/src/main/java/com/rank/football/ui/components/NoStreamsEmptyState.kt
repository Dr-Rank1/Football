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
import androidx.compose.ui.geometry.Offset
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

/**
 * Empty state when nothing is streamable.
 * [catalogHasStreams] = true → off-season / between matchdays.
 * [catalogHasStreams] = false → catalog empty or not configured (honest ops message).
 */
@Composable
fun NoStreamsEmptyState(
    context: NoStreamsContext,
    modifier: Modifier = Modifier,
    catalogHasStreams: Boolean = true,
    compact: Boolean = false,
    onBrowseFixtures: (() -> Unit)? = null,
    onBrowseLeagues: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val offSeason = catalogHasStreams
    val titleRes = if (offSeason) R.string.empty_streams_title else R.string.empty_catalog_title
    val bodyRes = when {
        !offSeason -> R.string.empty_catalog_body
        context == NoStreamsContext.HOME -> R.string.empty_streams_home_body
        context == NoStreamsContext.LIVE -> R.string.empty_streams_live_body
        context == NoStreamsContext.FIXTURES -> R.string.empty_streams_fixtures_body
        else -> R.string.empty_streams_leagues_body
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
            text = stringResource(titleRes),
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
            EmptyStateTips(offSeason = offSeason)
            Spacer(modifier = Modifier.height(20.dp))

            if (offSeason && (onBrowseFixtures != null || onBrowseLeagues != null)) {
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
    val line = PitchGreen.copy(alpha = 0.35f)
    val pitch = CardDark
    Box(
        modifier = Modifier
            .size(width = 168.dp, height = 105.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(pitch)
            .border(1.dp, line.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(105.dp).padding(8.dp)) {
            val stroke = 2f
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = line,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )
            drawLine(line, Offset(w / 2f, 0f), Offset(w / 2f, h), stroke)
            drawCircle(line, radius = h * 0.18f, center = Offset(w / 2f, h / 2f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            drawCircle(line, radius = 3f, center = Offset(w / 2f, h / 2f))
            val boxW = w * 0.2f
            val boxH = h * 0.5f
            drawRect(line, Offset(0f, (h - boxH) / 2f), androidx.compose.ui.geometry.Size(boxW, boxH), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            drawRect(line, Offset(w - boxW, (h - boxH) / 2f), androidx.compose.ui.geometry.Size(boxW, boxH), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
        }
    }
}

@Composable
private fun EmptyStateTips(offSeason: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StadiumBlack.copy(alpha = 0.4f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (offSeason) {
            TipRow(stringResource(R.string.empty_streams_tip_fixtures))
            TipRow(stringResource(R.string.empty_streams_tip_leagues))
            TipRow(stringResource(R.string.empty_streams_tip_notify))
        } else {
            TipRow(stringResource(R.string.empty_catalog_tip_refresh))
            TipRow(stringResource(R.string.empty_catalog_tip_later))
            TipRow(stringResource(R.string.empty_catalog_tip_notify))
        }
    }
}

@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", color = PitchGreen, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = TextGrey, style = MaterialTheme.typography.bodySmall)
    }
}
