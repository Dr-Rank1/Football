package com.rank.football.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.ui.theme.GoalStreamTheme
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.workers.WidgetUpdateWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Configuration screen shown when a user adds the GoalStream home widget. */
class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val initialTheme = runBlocking { WidgetPreferenceStore.theme(this@WidgetConfigureActivity).first() }
        val initialMax = runBlocking { WidgetPreferenceStore.maxMatches(this@WidgetConfigureActivity).first() }
        val initialLeague = runBlocking { WidgetPreferenceStore.leagueFilter(this@WidgetConfigureActivity).first() }

        setContent {
            GoalStreamTheme {
                WidgetConfigureScreen(
                    initialTheme = initialTheme,
                    initialMaxMatches = initialMax,
                    initialLeagueFilter = initialLeague,
                    onCancel = { finish() },
                    onSave = { theme, maxMatches, leagueFilter ->
                        saveAndFinish(theme, maxMatches, leagueFilter)
                    }
                )
            }
        }
    }

    /** Persists widget preferences and returns success to the widget host. */
    private fun saveAndFinish(theme: String, maxMatches: Int, leagueFilter: String) {
        runBlocking {
            WidgetPreferenceStore.setTheme(this@WidgetConfigureActivity, theme)
            WidgetPreferenceStore.setMaxMatches(this@WidgetConfigureActivity, maxMatches)
            WidgetPreferenceStore.setLeagueFilter(this@WidgetConfigureActivity, leagueFilter.trim())
        }
        WidgetUpdateWorker.enqueue(this)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigureScreen(
    initialTheme: String,
    initialMaxMatches: Int,
    initialLeagueFilter: String,
    onCancel: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    val themes = listOf("stadium_dark", "pitch_light", "classic_white")
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var maxMatches by remember { mutableFloatStateOf(initialMaxMatches.toFloat()) }
    var leagueFilter by remember { mutableStateOf(initialLeagueFilter) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = StadiumBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.widget_configure_title), color = TextWhite) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.widget_theme_label), color = TextWhite, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                themes.forEach { theme ->
                    FilterChip(
                        selected = selectedTheme == theme,
                        onClick = { selectedTheme = theme },
                        label = { Text(theme.replace('_', ' '), color = TextWhite) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PitchGreen,
                            selectedLabelColor = TextWhite
                        )
                    )
                }
            }

            Text(stringResource(R.string.widget_max_matches_label), color = TextWhite, style = MaterialTheme.typography.titleMedium)
            Text("${maxMatches.toInt()} matches", color = TextGrey)
            Slider(
                value = maxMatches,
                onValueChange = { maxMatches = it },
                valueRange = 3f..10f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = leagueFilter,
                onValueChange = { leagueFilter = it },
                label = { Text(stringResource(R.string.widget_league_filter_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(stringResource(R.string.widget_league_filter_hint), color = TextGrey)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel), color = TextGrey)
                }
                Button(
                    onClick = {
                        scope.launch {
                            onSave(selectedTheme, maxMatches.toInt(), leagueFilter)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.widget_configure_save))
                }
            }
        }
    }
}
