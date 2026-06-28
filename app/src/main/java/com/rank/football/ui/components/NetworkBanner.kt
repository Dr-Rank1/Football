package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.TextWhite

@Composable
fun NetworkBanner(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    Text(
        text = stringResource(R.string.no_internet),
        color = TextWhite,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(LiveRed)
            .padding(vertical = 6.dp)
    )
}
