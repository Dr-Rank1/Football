package com.rank.football.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.TextGrey

/**
 * Tiny goals scored (green) / conceded (red) spark bars for standings.
 * Uses cumulative-style fake trend from GF/GA magnitudes when form history is absent.
 */
@Composable
fun GoalsSparkline(
    goalsFor: Int,
    goalsAgainst: Int,
    modifier: Modifier = Modifier
) {
    val gf = goalsFor.coerceAtLeast(0)
    val ga = goalsAgainst.coerceAtLeast(0)
    val max = maxOf(gf, ga, 1).toFloat()

    // 5 micro-bars approximating trend from totals
    val scored = listOf(
        (gf * 0.5f / max),
        (gf * 0.65f / max),
        (gf * 0.8f / max),
        (gf * 0.9f / max),
        (gf / max)
    )
    val conceded = listOf(
        (ga * 0.55f / max),
        (ga * 0.7f / max),
        (ga * 0.75f / max),
        (ga * 0.85f / max),
        (ga / max)
    )

    Canvas(modifier = modifier.width(36.dp).height(18.dp)) {
        val barW = size.width / 11f
        val gap = barW * 0.15f
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
        // baseline
        drawLine(
            color = TextGrey.copy(alpha = 0.25f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1f
        )
    }
}
