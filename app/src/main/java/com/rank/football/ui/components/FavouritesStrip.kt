package com.rank.football.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.data.local.FavoriteTeam
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

@Composable
fun FavouritesStrip(
    favorites: List<FavoriteTeam>,
    liveFixtures: List<FixtureItem>,
    selectedTeamId: Int?,
    onTeamClick: (Int?) -> Unit,
    modifier: Modifier = Modifier
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    onTeamClick(if (selected) null else team.teamId)
                }
            ) {
                Box {
                    AsyncImage(
                        model = team.teamLogo,
                        contentDescription = team.teamName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) PitchGreen else TextGrey.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .background(TextGrey.copy(alpha = 0.15f)),
                        contentScale = ContentScale.Fit
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
                    text = team.teamName.split(" ").firstOrNull().orEmpty(),
                    color = if (selected) PitchGreen else TextGrey.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
