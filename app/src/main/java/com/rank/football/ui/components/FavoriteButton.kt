package com.rank.football.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.rank.football.data.local.FavoriteTeam
import com.rank.football.data.repository.FavoritesRepository
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.TextGrey
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(
    teamId: Int?,
    teamName: String,
    teamLogo: String?,
    leagueId: Int,
    leagueName: String,
    favoritesRepository: FavoritesRepository,
    modifier: Modifier = Modifier
) {
    if (teamId == null) return
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isFavorite by favoritesRepository.isFavorite(teamId).collectAsState(initial = false)
    val color by animateColorAsState(
        targetValue = if (isFavorite) LiveRed else TextGrey,
        animationSpec = tween(300),
        label = "fav_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = tween(200),
        label = "fav_scale"
    )

    Icon(
        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = null,
        tint = color,
        modifier = modifier
            .scale(scale)
            .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch {
                        favoritesRepository.toggleFavorite(
                            FavoriteTeam(
                                teamId = teamId,
                                teamName = teamName,
                                teamLogo = teamLogo ?: "",
                                leagueId = leagueId,
                                leagueName = leagueName
                            ),
                            isFavorite
                        )
                    }
                }
            )
}
