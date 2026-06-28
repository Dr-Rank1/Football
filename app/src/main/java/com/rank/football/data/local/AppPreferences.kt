package com.rank.football.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AppPreferences {
    private val CONSENT_GIVEN = booleanPreferencesKey("consent_given")
    private val DISPLAY_NAME = stringPreferencesKey("display_name")
    private val APP_LANGUAGE = stringPreferencesKey("app_language")
    private val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
    private val PRELOAD_NEXT = booleanPreferencesKey("preload_next")
    private val MOBILE_DATA_WARNING = booleanPreferencesKey("mobile_data_warning")
    private val BACKGROUND_AUDIO = booleanPreferencesKey("background_audio")
    private val MATCH_REMINDERS = booleanPreferencesKey("match_reminders")
    private val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
    private val GOAL_ALERTS = booleanPreferencesKey("goal_alerts")
    private val FAVORITE_ONLY_NOTIF = booleanPreferencesKey("favorite_only_notif")
    private val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
    private val CROWD_SOUNDS = booleanPreferencesKey("crowd_sounds")
    private val INSTALL_DATE = longPreferencesKey("install_date")
    private val PREFERRED_AUDIO_LANG = stringPreferencesKey("preferred_audio_lang")
    private val REVIEW_SHOWN_KEY = booleanPreferencesKey("in_app_review_shown")
    private val NEWS_SUMMARY_HOUR = stringPreferencesKey("news_summary_hour")
    private val NOTIF_VOLUME = intPreferencesKey("notif_volume")
    private val NOTIF_QUIET_HOURS = booleanPreferencesKey("notif_quiet_hours")
    private val HALFTIME_ALERTS = booleanPreferencesKey("halftime_alerts")
    private val KICKOFF_ALERTS = booleanPreferencesKey("kickoff_alerts")

    fun consentGiven(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[CONSENT_GIVEN] ?: false }

    suspend fun setConsentGiven(context: Context) {
        OnboardingPreference.dataStore(context).edit { it[CONSENT_GIVEN] = true }
    }

    fun displayName(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[DISPLAY_NAME] ?: generateFanName() }

    suspend fun setDisplayName(context: Context, name: String) {
        OnboardingPreference.dataStore(context).edit { it[DISPLAY_NAME] = name }
    }

    fun appLanguage(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[APP_LANGUAGE] ?: "en" }

    suspend fun setAppLanguage(context: Context, lang: String) {
        OnboardingPreference.dataStore(context).edit { it[APP_LANGUAGE] = lang }
    }

    fun defaultQuality(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[DEFAULT_QUALITY] ?: "Auto" }

    suspend fun setDefaultQuality(context: Context, quality: String) {
        OnboardingPreference.dataStore(context).edit { it[DEFAULT_QUALITY] = quality }
    }

    fun preloadNext(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[PRELOAD_NEXT] ?: true }

    suspend fun setPreloadNext(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[PRELOAD_NEXT] = enabled }
    }

    fun mobileDataWarning(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[MOBILE_DATA_WARNING] ?: true }

    suspend fun setMobileDataWarning(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[MOBILE_DATA_WARNING] = enabled }
    }

    fun backgroundAudio(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[BACKGROUND_AUDIO] ?: false }

    suspend fun setBackgroundAudio(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[BACKGROUND_AUDIO] = enabled }
    }

    fun matchReminders(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[MATCH_REMINDERS] ?: true }

    suspend fun setMatchReminders(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[MATCH_REMINDERS] = enabled }
    }

    fun reminderMinutes(context: Context): Flow<Int> =
        OnboardingPreference.dataStore(context).data.map { it[REMINDER_MINUTES] ?: 15 }

    suspend fun setReminderMinutes(context: Context, minutes: Int) {
        OnboardingPreference.dataStore(context).edit { it[REMINDER_MINUTES] = minutes }
    }

    fun goalAlerts(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[GOAL_ALERTS] ?: true }

    fun favoriteOnlyNotifications(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[FAVORITE_ONLY_NOTIF] ?: true }

    fun reduceAnimations(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[REDUCE_ANIMATIONS] ?: false }

    fun crowdSounds(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[CROWD_SOUNDS] ?: false }

    suspend fun setCrowdSounds(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[CROWD_SOUNDS] = enabled }
    }

    fun reviewShown(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[REVIEW_SHOWN_KEY] ?: false }

    suspend fun setReviewShown(context: Context) {
        OnboardingPreference.dataStore(context).edit { it[REVIEW_SHOWN_KEY] = true }
    }

    suspend fun ensureInstallDate(context: Context) {
        OnboardingPreference.dataStore(context).edit { prefs ->
            if (prefs[INSTALL_DATE] == null) {
                prefs[INSTALL_DATE] = System.currentTimeMillis()
            }
        }
    }

    fun installDate(context: Context): Flow<Long> =
        OnboardingPreference.dataStore(context).data.map { it[INSTALL_DATE] ?: System.currentTimeMillis() }

    fun preferredAudioLanguage(context: Context): Flow<String> =
        OnboardingPreference.dataStore(context).data.map { it[PREFERRED_AUDIO_LANG] ?: "en" }

    suspend fun setPreferredAudioLanguage(context: Context, lang: String) {
        OnboardingPreference.dataStore(context).edit { it[PREFERRED_AUDIO_LANG] = lang }
    }

    fun newsSummaryHour(context: Context): Flow<String?> =
        OnboardingPreference.dataStore(context).data.map { it[NEWS_SUMMARY_HOUR] }

    /** Persists the hourly AI news digest for NewsScreen cache hits. */
    suspend fun setNewsSummaryHour(context: Context, summary: String) {
        OnboardingPreference.dataStore(context).edit { it[NEWS_SUMMARY_HOUR] = summary }
    }

    fun notificationVolume(context: Context): Flow<Int> =
        OnboardingPreference.dataStore(context).data.map { it[NOTIF_VOLUME] ?: 80 }

    /** Sets notification volume slider value from 0–100. */
    suspend fun setNotificationVolume(context: Context, volume: Int) {
        OnboardingPreference.dataStore(context).edit { it[NOTIF_VOLUME] = volume.coerceIn(0, 100) }
    }

    fun quietHoursEnabled(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[NOTIF_QUIET_HOURS] ?: false }

    /** Toggles quiet-hours suppression for non-critical alerts. */
    suspend fun setQuietHoursEnabled(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[NOTIF_QUIET_HOURS] = enabled }
    }

    fun halftimeAlerts(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[HALFTIME_ALERTS] ?: true }

    /** Enables or disables halftime score notifications. */
    suspend fun setHalftimeAlerts(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[HALFTIME_ALERTS] = enabled }
    }

    fun kickoffAlerts(context: Context): Flow<Boolean> =
        OnboardingPreference.dataStore(context).data.map { it[KICKOFF_ALERTS] ?: true }

    /** Enables or disables kickoff reminder notifications. */
    suspend fun setKickoffAlerts(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[KICKOFF_ALERTS] = enabled }
    }

    suspend fun setGoalAlerts(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[GOAL_ALERTS] = enabled }
    }

    suspend fun setFavoriteOnlyNotifications(context: Context, enabled: Boolean) {
        OnboardingPreference.dataStore(context).edit { it[FAVORITE_ONLY_NOTIF] = enabled }
    }

    /** Generates a random anonymous fan display name. */
    fun generateFanName(): String = "Fan${(1000..9999).random()}"
}
