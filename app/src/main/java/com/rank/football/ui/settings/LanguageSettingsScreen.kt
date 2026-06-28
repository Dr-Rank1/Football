package com.rank.football.ui.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.rank.football.R
import com.rank.football.data.local.AppPreferences
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.launch

data class AppLanguage(val tag: String, val flag: String, val labelRes: Int)

private val languages = listOf(
    AppLanguage("en", "🇬🇧", R.string.lang_english),
    AppLanguage("sw", "🇰🇪", R.string.lang_swahili),
    AppLanguage("fr", "🇫🇷", R.string.lang_french),
    AppLanguage("ar", "🇸🇦", R.string.lang_arabic),
    AppLanguage("pt", "🇵🇹", R.string.lang_portuguese)
)

/** Lists supported languages and applies locale via AppCompatDelegate. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = StadiumBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_language), color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextWhite)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            languages.forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                AppPreferences.setAppLanguage(context, lang.tag)
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(lang.tag)
                                )
                                (context as? Activity)?.recreate()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(lang.flag, modifier = Modifier.padding(end = 12.dp))
                    Text(stringResource(lang.labelRes), color = TextWhite)
                }
            }
        }
    }
}
