package com.rank.football.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.data.local.FavoriteTeam
import com.rank.football.data.model.FixtureItem
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/** Favourite-team crest rail with a trailing "+ Add" ghost tile (UI reference). */
@Composable
fun FavouritesStrip(
    favorites: List<FavoriteTeam>,
    liveFixtures: List<FixtureItem>,
    selectedTeamId: Int?,
    onTeamClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    if (favorites.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(favorites, key = { it.teamId }) { team ->
            val isLive = liveFixtures.any {
                it.teams.home.id == team.teamId || it.teams.away.id == team.teamId
            }
            val selected = selectedTeamId == team.teamId
            val selShape = if (team.teamLogo.isNullOrBlank()) {
                RoundedCornerShape(10.dp)
            } else {
                CircleShape
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    onTeamClick(if (selected) null else team.teamId)
                }
            ) {
                Box {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .size(46.dp)
                                .border(2.dp, PitchGreen, selShape)
                        )
                    }
                    TeamCrest(
                        name = team.teamName,
                        logo = team.teamLogo,
                        size = 44.dp
                    )
                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .border(2.dp, StadiumBlack, CircleShape)
                                .background(LiveRed, CircleShape)
                        )
                    }
                }
                Text(
                    text = teamCodeOf(team.teamName),
                    color = if (selected) PitchGreen else TextGrey.copy(alpha = 0.7f),
                    fontFamily = DmSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        item(key = "add_team") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onAddClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = TextGrey.copy(alpha = 0.4f),
                                cornerRadius = CornerRadius(10.dp.toPx()),
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = TextGrey,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                Text(
                    text = "Add",
                    color = TextGrey,
                    fontFamily = DmSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
