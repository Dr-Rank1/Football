package com.rank.football.ui.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rank.football.data.local.AppDatabase
import com.rank.football.performance.AppPerformanceMonitor
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.flow.first

private val DEBUG_TABS = listOf("Events", "Ads", "AI", "Streams", "Perf")

/** Debug dashboard with tabs for events, ads, AI, streams, and performance. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen() {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var lines by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(tab) {
        val db = AppDatabase.getInstance(context)
        lines = when (DEBUG_TABS[tab]) {
            "Events" -> db.analyticsDao().recent(50).map { "${it.eventName} @ ${it.timestamp}" }
            "Ads" -> db.adImpressionDao().byDay(java.time.LocalDate.now().toString()).first()
                .map { "${it.adType}: ${it.count}" }
            "AI" -> listOf("Gemini Flash/Pro via GeminiClient", "Rate limiter active")
            "Streams" -> listOf("Adaptive bitrate enabled", "HLS playback")
            "Perf" -> {
                AppPerformanceMonitor.logMemoryUsage(context)
                listOf("Memory snapshot logged to LocalAnalytics")
            }
            else -> emptyList()
        }
    }

    Scaffold(
        containerColor = StadiumBlack,
        topBar = { TopAppBar(title = { Text("Analytics Dashboard", color = TextWhite) }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                DEBUG_TABS.forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                }
            }
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(lines) { line ->
                    Text(line, color = TextGrey, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
