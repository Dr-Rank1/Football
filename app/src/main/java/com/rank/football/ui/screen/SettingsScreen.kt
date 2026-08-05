package com.rank.football.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.football.BuildConfig
import com.rank.football.R
import com.rank.football.data.local.AppPreferences
import com.rank.football.security.DeviceSecurity
import com.rank.football.ui.components.ProtoScreenTitle
import com.rank.football.ui.components.SettingsUpgradeCard
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageClick: () -> Unit,
    onDebugClick: () -> Unit = {},
    onRevenueClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reminders by AppPreferences.matchReminders(context).collectAsState(initial = true)
    val goalAlerts by AppPreferences.goalAlerts(context).collectAsState(initial = true)
    val shareText = stringResource(R.string.settings_share_text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ProtoScreenTitle(title = stringResource(R.string.settings_title))

        Spacer(modifier = Modifier.height(14.dp))

        SettingsUpgradeCard(onUpgrade = onRevenueClick)

        Spacer(modifier = Modifier.height(18.dp))

        SettingsGroupHeader(label = stringResource(R.string.settings_preferences))
        SettingsGroup {
            SettingsLink(
                icon = Icons.Default.Language,
                label = stringResource(R.string.settings_language),
                value = "English"
            ) { onLanguageClick() }
            SettingsDivider()
            SettingsToggle(
                icon = Icons.Default.Notifications,
                label = stringResource(R.string.settings_notifications_short),
                checked = reminders
            ) {
                scope.launch { AppPreferences.setMatchReminders(context, it) }
            }
            SettingsDivider()
            SettingsToggle(
                icon = Icons.Default.SportsSoccer,
                label = stringResource(R.string.settings_goal_alerts),
                checked = goalAlerts
            ) {
                scope.launch { AppPreferences.setGoalAlerts(context, it) }
            }
            SettingsDivider()
            SettingsLink(
                icon = Icons.Default.DarkMode,
                label = stringResource(R.string.settings_dark_mode),
                value = stringResource(R.string.settings_dark_mode_value)
            ) {}
        }

        Spacer(modifier = Modifier.height(18.dp))

        SettingsGroupHeader(label = stringResource(R.string.settings_app))
        SettingsGroup {
            SettingsLink(
                icon = Icons.Default.Star,
                label = stringResource(R.string.settings_rate)
            ) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
            }
            SettingsDivider()
            SettingsLink(
                icon = Icons.Default.Share,
                label = stringResource(R.string.settings_share)
            ) {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(share, null))
            }
            SettingsDivider()
            SettingsLink(
                icon = Icons.Default.Info,
                label = stringResource(R.string.settings_about),
                value = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME)
            ) {}
            SettingsDivider()
            SettingsLink(
                icon = Icons.Default.Lock,
                label = stringResource(R.string.settings_privacy)
            ) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://goalstream.app/privacy")))
            }
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(18.dp))
            SettingsGroupHeader(label = "DEVELOPER")
            SettingsGroup {
                SettingsLink(
                    icon = Icons.Default.BugReport,
                    label = stringResource(R.string.settings_debug)
                ) { onDebugClick() }
                SettingsDivider()
                SettingsLink(
                    icon = Icons.Default.Paid,
                    label = stringResource(R.string.revenue_stats)
                ) { onRevenueClick() }
            }
        }

        DeviceSecurity.rootWarning()?.let { warning ->
            Text(
                warning,
                color = LiveRed,
                fontFamily = DmSans,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingsGroupHeader(label: String) {
    Text(
        text = label,
        color = TextGrey,
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 2.dp, bottom = 7.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(CardDark)
            .border(1.dp, TextWhite.copy(alpha = 0.055f), RoundedCornerShape(13.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = TextWhite.copy(alpha = 0.05f),
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGrey,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            color = TextWhite,
            fontFamily = DmSans,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StadiumBlack,
                checkedTrackColor = PitchGreen,
                uncheckedThumbColor = TextGrey,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}

@Composable
private fun SettingsLink(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGrey,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            color = TextWhite,
            fontFamily = DmSans,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                color = TextGrey,
                fontFamily = DmSans,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = "›", color = TextGrey.copy(alpha = 0.5f), fontSize = 16.sp)
    }
}
