package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/** Competition group header with match count (UI reference fixtures). */
@Composable
fun LeagueHeader(
    leagueName: String,
    leagueLogo: String?,
    country: String? = null,
    matchCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(CardDark)
            .border(1.dp, TextWhite.copy(alpha = 0.055f), shape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!leagueLogo.isNullOrBlank()) {
            AsyncImage(
                model = leagueLogo,
                contentDescription = leagueName,
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = leagueName.uppercase(),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (matchCount != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$matchCount match" + if (matchCount != 1) "es" else "",
                color = TextGrey,
                fontFamily = DmSans,
                fontSize = 11.sp
            )
        }
    }
}
