package com.rank.football.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

@Composable
fun GoalToastHost(
    onOpenMatch: (Int) -> Unit,
    hostModifier: Modifier = Modifier
) {
    var current by remember { mutableStateOf<GoalAlert?>(null) }

    LaunchedEffect(Unit) {
        GoalAlertBus.alerts.collect { alert ->
            current = alert
        }
    }

    AnimatedVisibility(
        visible = current != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = hostModifier
    ) {
        current?.let { alert ->
            GoalToastCard(
                alert = alert,
                onDismiss = { current = null },
                onClick = {
                    onOpenMatch(alert.fixtureId)
                    current = null
                }
            )
        }
    }
}

@Composable
private fun GoalToastCard(
    alert: GoalAlert,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(alert) {
        progress.snapTo(1f)
        progress.animateTo(0f, animationSpec = tween(4500, easing = LinearEasing))
        onDismiss()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PitchGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .background(ColorGoalToastBg)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PitchGreen.copy(alpha = 0.2f))
                    .border(1.dp, PitchGreen.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = PitchGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "GOAL! · ${alert.minute}'",
                    color = PitchGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${alert.scorer} · ${alert.team}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "${alert.score} · ${alert.competition}",
                    color = TextGrey,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextGrey,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(StadiumBlack.copy(alpha = 0.4f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .graphicsLayer {
                        scaleX = progress.value
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                    .background(PitchGreen)
            )
        }
    }
}

private val ColorGoalToastBg = androidx.compose.ui.graphics.Color(0xFF0D1F14)
