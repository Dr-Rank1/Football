package com.rank.football.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rank.football.data.local.OnboardingPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists home-screen widget customization options. */
object WidgetPreferenceStore {

    private val WIDGET_THEME = stringPreferencesKey("widget_theme_v5")
    private val WIDGET_MAX_MATCHES = intPreferencesKey("widget_max_matches_v5")
    private val WIDGET_LEAGUE_FILTER = stringPreferencesKey("widget_league_filter_v5")

    /** Exposes the selected widget color theme name. */
    fun theme(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[WIDGET_THEME] ?: "stadium_dark" }

    /** Exposes how many match rows the large widget should show. */
    fun maxMatches(context: Context): Flow<Int> =
        OnboardingPreference.dataStore(context).data.map { it[WIDGET_MAX_MATCHES] ?: 6 }

    /** Exposes optional league name filter for widget scores. */
    fun leagueFilter(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[WIDGET_LEAGUE_FILTER] ?: "" }

    /** Saves the widget theme preference. */
    suspend fun setTheme(context: Context, theme: String) {
        OnboardingPreference.dataStore(context).edit { it[WIDGET_THEME] = theme }
    }

    /** Saves the maximum number of matches shown on the widget. */
    suspend fun setMaxMatches(context: Context, count: Int) {
        OnboardingPreference.dataStore(context).edit { it[WIDGET_MAX_MATCHES] = count.coerceIn(3, 10) }
    }

    /** Saves the league filter applied to widget live scores. */
    suspend fun setLeagueFilter(context: Context, league: String) {
        OnboardingPreference.dataStore(context).edit { it[WIDGET_LEAGUE_FILTER] = league }
    }
}
