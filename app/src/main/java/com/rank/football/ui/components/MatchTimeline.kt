package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.data.model.FixtureEventItem
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

@Composable
fun MatchTimeline(
    currentMinute: Int,
    events: List<FixtureEventItem>,
    onMarkerClick: (minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxMinute: Int = 90
) {
    val goals = events.filter { it.type.contains("Goal", ignoreCase = true) }
    val cards = events.filter {
        it.type.contains("Card", ignoreCase = true) ||
            it.detail?.contains("Card", ignoreCase = true) == true
    }
    val pct = (currentMinute.coerceIn(0, maxMinute).toFloat() / maxMinute)

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "TIMELINE",
                color = TextGrey.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                "$currentMinute' / ${maxMinute}'",
                color = TextGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(TextWhite.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
            )
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(listOf(PitchGreen, NeonGreen)),
                        RoundedCornerShape(3.dp)
                    )
            )
            // Half marker
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 1.dp, height = 12.dp)
                    .background(TextWhite.copy(alpha = 0.15f))
            )
            // Goal markers
            goals.forEach { g ->
                val m = g.time.elapsed ?: return@forEach
                val xFrac = (m.toFloat() / maxMinute).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xFrac)
                        .align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 6.dp)
                            .size(12.dp)
                            .background(GoalYellow, CircleShape)
                            .clickable { onMarkerClick(m) }
                    )
                }
            }
            // Card markers
            cards.forEach { c ->
                val m = c.time.elapsed ?: return@forEach
                val xFrac = (m.toFloat() / maxMinute).coerceIn(0f, 1f)
                val color = if (c.detail?.contains("Red", true) == true) LiveRed else Color(0xFFFFEB3B)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xFrac)
                        .align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 2.dp, y = (-8).dp)
                            .size(width = 4.dp, height = 10.dp)
                            .background(color, RoundedCornerShape(1.dp))
                            .clickable { onMarkerClick(m) }
                    )
                }
            }
            // Playhead
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 7.dp)
                        .size(14.dp)
                        .background(TextWhite, CircleShape)
                        .padding(2.dp)
                        .background(PitchGreen, CircleShape)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0'", color = TextGrey.copy(alpha = 0.35f), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("45'", color = TextGrey.copy(alpha = 0.35f), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("90'", color = TextGrey.copy(alpha = 0.35f), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}
