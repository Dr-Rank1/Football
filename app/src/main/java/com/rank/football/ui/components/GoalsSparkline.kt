package com.rank.football.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/**
 * Standings form + GF/GA cell.
 * Prefers real last-5 form dots when [form] is present; otherwise shows GF/GA spark bars.
 */
@Composable
fun GoalsSparkline(
    goalsFor: Int,
    goalsAgainst: Int,
    panelModifier: Modifier = Modifier,
    form: String = ""
) {
    val cleaned = form.filter { it in "WwDdLl" }.takeLast(5)
    if (cleaned.isNotEmpty()) {
        Column(
            modifier = panelModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                cleaned.forEach { ch ->
                    val color = when (ch.uppercaseChar()) {
                        'W' -> PitchGreen
                        'D' -> GoalYellow
                        else -> LiveRed
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            Text(
                text = "$goalsFor/$goalsAgainst",
                color = TextGrey,
                style = MaterialTheme.typography.labelSmall
            )
        }
        return
    }

    val gf = goalsFor.coerceAtLeast(0)
    val ga = goalsAgainst.coerceAtLeast(0)
    val max = maxOf(gf, ga, 1).toFloat()

    Column(
        modifier = panelModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.width(36.dp).height(12.dp)) {
            val barW = size.width / 11f
            val gap = barW * 0.15f
            val scored = listOf(0.5f, 0.65f, 0.8f, 0.9f, 1f).map { (gf * it / max) }
            val conceded = listOf(0.55f, 0.7f, 0.75f, 0.85f, 1f).map { (ga * it / max) }
            scored.forEachIndexed { i, h ->
                val x = i * (barW * 2 + gap)
                val barH = (h.coerceIn(0.12f, 1f)) * size.height
                drawRoundRect(
                    color = PitchGreen.copy(alpha = 0.85f),
                    topLeft = Offset(x, size.height - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(1f, 1f)
                )
                val ch = (conceded.getOrElse(i) { 0.2f }.coerceIn(0.1f, 1f)) * size.height * 0.7f
                drawRoundRect(
                    color = LiveRed.copy(alpha = 0.7f),
                    topLeft = Offset(x + barW + 1f, size.height - ch),
                    size = Size(barW * 0.7f, ch),
                    cornerRadius = CornerRadius(1f, 1f)
                )
            }
        }
        Text(
            text = "$gf/$ga",
            color = TextWhite.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
