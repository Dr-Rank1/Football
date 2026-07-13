package com.rank.football.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.TextWhite

@Composable
fun LiveBadge(
    minute: Int? = null,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val pulse = rememberInfiniteTransition(label = "live_dot")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "dot_scale"
    )
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "dot_alpha"
    )
    val label = if (minute != null) "LIVE · ${minute}'" else "LIVE"

    Row(
        modifier = modifier
            .background(LiveRed, RoundedCornerShape(50))
            .padding(
                horizontal = if (large) 10.dp else 8.dp,
                vertical = if (large) 4.dp else 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(if (large) 6.dp else 5.dp)
                .scale(scale)
                .graphicsLayer { this.alpha = alpha }
                .background(TextWhite, CircleShape)
        )
        Text(
            text = label,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = if (large) 11.sp else 9.sp,
            letterSpacing = 0.8.sp
        )
    }
}
