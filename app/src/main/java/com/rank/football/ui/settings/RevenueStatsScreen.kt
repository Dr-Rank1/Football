package com.rank.football.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rank.football.monetization.AdRevenueTracker
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite

/** Debug-only estimated ad revenue chart drawn with Canvas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueStatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val totals by AdRevenueTracker.dailyTotals(context).collectAsState(initial = emptyMap())

    Scaffold(
        containerColor = StadiumBlack,
        topBar = {
            TopAppBar(
                title = { Text("Revenue Stats", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextWhite)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Estimated earnings — actual AdMob dashboard may differ", color = TextGrey)
            val max = totals.values.maxOrNull()?.toFloat()?.coerceAtLeast(0.01f) ?: 1f
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 16.dp)) {
                val barWidth = size.width / (totals.size.coerceAtLeast(1) * 2)
                totals.entries.forEachIndexed { i, (_, value) ->
                    val h = (value / max * size.height).toFloat()
                    drawRect(
                        color = PitchGreen,
                        topLeft = androidx.compose.ui.geometry.Offset(i * barWidth * 2, size.height - h),
                        size = Size(barWidth, h)
                    )
                }
            }
            totals.forEach { (type, value) ->
                Text("${type.name}: $${"%.4f".format(value)}", color = TextWhite)
            }
        }
    }
}
