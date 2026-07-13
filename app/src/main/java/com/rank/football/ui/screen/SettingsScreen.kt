package com.rank.football.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rank.football.BuildConfig
import com.rank.football.R
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.rank.football.security.DeviceSecurity
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onDebugClick: () -> Unit = {},
    onRevenueClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preload by AppPreferences.preloadNext(context).collectAsState(initial = true)
    val mobileWarn by AppPreferences.mobileDataWarning(context).collectAsState(initial = true)
    val bgAudio by AppPreferences.backgroundAudio(context).collectAsState(initial = false)
    val reminders by AppPreferences.matchReminders(context).collectAsState(initial = true)
    var confirmClear by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = StadiumBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SectionHeader(stringResource(R.string.settings_streaming))
            SettingsToggle(stringResource(R.string.settings_preload), preload) {
                scope.launch { AppPreferences.setPreloadNext(context, it) }
            }
            SettingsToggle(stringResource(R.string.settings_mobile_warning), mobileWarn) {
                scope.launch { AppPreferences.setMobileDataWarning(context, it) }
            }
            SettingsToggle(stringResource(R.string.settings_background_audio), bgAudio) {
                scope.launch { AppPreferences.setBackgroundAudio(context, it) }
            }

            SectionHeader(stringResource(R.string.settings_notifications))
            SettingsToggle(stringResource(R.string.settings_match_reminders), reminders) {
                scope.launch { AppPreferences.setMatchReminders(context, it) }
            }
            val goalAlerts by AppPreferences.goalAlerts(context).collectAsState(initial = true)
            val favOnly by AppPreferences.favoriteOnlyNotifications(context).collectAsState(initial = true)
            SettingsToggle(stringResource(R.string.settings_goal_alerts), goalAlerts) {
                scope.launch { AppPreferences.setGoalAlerts(context, it) }
            }
            SettingsToggle(stringResource(R.string.settings_favorite_only), favOnly) {
                scope.launch { AppPreferences.setFavoriteOnlyNotifications(context, it) }
            }

            SectionHeader(stringResource(R.string.settings_appearance))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageClick() }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_language), color = TextWhite, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.settings_language_value), color = TextGrey)
            }

            SectionHeader(stringResource(R.string.settings_account))
            TextButton(onClick = { confirmClear = "history" }) {
                Text(stringResource(R.string.settings_clear_history), color = TextGrey)
            }
            TextButton(onClick = { confirmClear = "favorites" }) {
                Text(stringResource(R.string.settings_clear_favorites), color = TextGrey)
            }

            SectionHeader(stringResource(R.string.settings_about))
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                color = TextGrey,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://goalstream.app/privacy")))
            }) { Text(stringResource(R.string.settings_privacy), color = TextGrey) }
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
            }) { Text(stringResource(R.string.settings_rate), color = TextGrey) }
            TextButton(onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.settings_share_text))
                }
                context.startActivity(Intent.createChooser(share, null))
            }) { Text(stringResource(R.string.settings_share), color = TextGrey) }
            if (BuildConfig.DEBUG) {
                TextButton(onClick = onDebugClick) { Text(stringResource(R.string.settings_debug), color = TextGrey) }
                TextButton(onClick = onRevenueClick) { Text(stringResource(R.string.revenue_stats), color = TextGrey) }
            }
            DeviceSecurity.rootWarning()?.let { warning ->
                Text(warning, color = LiveRed, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    confirmClear?.let { type ->
        AlertDialog(
            onDismissRequest = { confirmClear = null },
            title = { Text(stringResource(R.string.confirm)) },
            text = { Text(stringResource(R.string.settings_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val db = AppDatabase.getInstance(context)
                        when (type) {
                            "history" -> db.watchHistoryDao().clearAll()
                            "favorites" -> db.favoritesDao().clearAll()
                        }
                        confirmClear = null
                    }
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = null }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = TextGrey,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextWhite, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
