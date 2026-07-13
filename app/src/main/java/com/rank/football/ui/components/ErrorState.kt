package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = stringResource(R.string.error_title)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, SurfaceDark, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (title != null) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                text = message,
                color = TextGrey,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PitchGreen,
                    contentColor = StadiumBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
