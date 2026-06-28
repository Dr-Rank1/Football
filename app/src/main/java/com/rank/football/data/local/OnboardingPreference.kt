package com.rank.football.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "goalstream_prefs")

object OnboardingPreference {

    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    private val RECENT_SEARCHES_V4 = stringPreferencesKey("recent_searches_v4")

    /** Exposes the shared preferences DataStore instance. */
    fun dataStore(context: Context): DataStore<Preferences> = context.dataStore

    fun isOnboardingComplete(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }

    suspend fun setOnboardingComplete(context: Context) {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }

    fun recentSearches(context: Context): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[RECENT_SEARCHES]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
        }

    suspend fun addRecentSearch(context: Context, query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(query) + current.filter { it != query }).take(10)
            prefs[RECENT_SEARCHES] = updated.joinToString("|")
            val v4Current = parseV4Entries(prefs[RECENT_SEARCHES_V4])
            val v4Updated = (listOf(query to System.currentTimeMillis()) + v4Current.filter { it.first != query })
                .take(10)
            prefs[RECENT_SEARCHES_V4] = v4Updated.joinToString("|") { "${it.first}:${it.second}" }
        }
    }

    /** Exposes recent searches with timestamps for grouped UI. */
    fun recentSearchesWithTimestamps(context: Context): Flow<List<RecentSearchEntry>> =
        context.dataStore.data.map { prefs ->
            val v4 = parseV4Entries(prefs[RECENT_SEARCHES_V4])
            if (v4.isNotEmpty()) {
                v4.map { RecentSearchEntry(it.first, it.second) }
            } else {
                (prefs[RECENT_SEARCHES]?.split("|")?.filter { it.isNotBlank() } ?: emptyList())
                    .map { RecentSearchEntry(it, System.currentTimeMillis()) }
            }
        }

    /** Removes one query from recent search history. */
    suspend fun removeRecentSearch(context: Context, query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
            prefs[RECENT_SEARCHES] = current.filter { it != query }.joinToString("|")
            val v4 = parseV4Entries(prefs[RECENT_SEARCHES_V4]).filter { it.first != query }
            prefs[RECENT_SEARCHES_V4] = v4.joinToString("|") { "${it.first}:${it.second}" }
        }
    }

    private fun parseV4Entries(raw: String?): List<Pair<String, Long>> =
        raw?.split("|")?.mapNotNull { part ->
            val idx = part.lastIndexOf(':')
            if (idx <= 0) return@mapNotNull null
            val q = part.substring(0, idx)
            val ts = part.substring(idx + 1).toLongOrNull() ?: return@mapNotNull null
            q to ts
        } ?: emptyList()
}

/** Timestamped recent search row for SearchScreen grouping. */
data class RecentSearchEntry(val query: String, val timestampMs: Long)
