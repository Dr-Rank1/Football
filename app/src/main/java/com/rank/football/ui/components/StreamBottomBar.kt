package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

data class StreamNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val liveCount: Int = -1
)

/** Custom bottom dock with stronger selected state and live pulse badge. */
@Composable
fun StreamBottomBar(
    items: List<StreamNavItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    barModifier: Modifier = Modifier
) {
    Column(
        barModifier
            .fillMaxWidth()
            .background(StadiumBlack)
            .border(width = 1.dp, color = TextWhite.copy(alpha = 0.06f), shape = RoundedCornerShape(0.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(item.route) }
                        .padding(vertical = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) PitchGreen else TextGrey,
                            modifier = Modifier.size(22.dp)
                        )
                        if (item.liveCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 14.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LiveRed)
                            )
                        }
                    }
                    Text(
                        text = item.label.uppercase(),
                        color = if (selected) PitchGreen else TextGrey,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(width = 16.dp, height = 2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(PitchGreen)
                        )
                    }
                }
            }
        }
    }
}
