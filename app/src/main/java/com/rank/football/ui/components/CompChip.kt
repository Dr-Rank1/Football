package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

@Composable
fun CompChip(
    name: String,
    leagueId: Int = 0,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accent = CompetitionColors.accent(name, leagueId)
    Text(
        text = name.uppercase(),
        color = accent.copy(alpha = 0.93f),
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = if (compact) 8.sp else 9.sp,
        letterSpacing = 0.6.sp,
        maxLines = 1,
        modifier = modifier
            .background(accent.copy(alpha = 0.13f), RoundedCornerShape(4.dp))
            .border(1.dp, accent.copy(alpha = 0.27f), RoundedCornerShape(4.dp))
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 1.dp else 2.dp
            )
    )
}
