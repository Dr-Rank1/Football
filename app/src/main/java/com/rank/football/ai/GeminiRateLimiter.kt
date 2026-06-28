package com.rank.football.ai

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rank.football.data.local.OnboardingPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Token-bucket rate limiter for Gemini Pro and Flash models, persisted in DataStore. */
class GeminiRateLimiter(private val context: Context) {

    enum class ModelTier { PRO, FLASH }

    private val mutex = Mutex()
    private val proTimestamps = mutableListOf<Long>()
    private val flashTimestamps = mutableListOf<Long>()

    companion object {
        private val PRO_TIMESTAMPS_KEY = stringPreferencesKey("gemini_pro_timestamps")
        private val FLASH_TIMESTAMPS_KEY = stringPreferencesKey("gemini_flash_timestamps")
        private val LAST_BACKOFF_KEY = longPreferencesKey("gemini_last_backoff_ms")
        private const val PRO_LIMIT = 5
        private const val FLASH_LIMIT = 20
        private const val WINDOW_MS = 60_000L
    }

    /** Waits until a slot is available for the given model tier, with exponential backoff. */
    suspend fun acquire(tier: ModelTier) {
        mutex.withLock {
            loadFromStore()
            pruneOld()
            val limit = if (tier == ModelTier.PRO) PRO_LIMIT else FLASH_LIMIT
            val list = if (tier == ModelTier.PRO) proTimestamps else flashTimestamps
            var backoff = 500L
            while (list.size >= limit) {
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(8000L)
                pruneOld()
            }
            list.add(System.currentTimeMillis())
            persist()
        }
    }

    /** Returns true when the tier is at capacity and caller should show a waiting state. */
    suspend fun isQueued(tier: ModelTier): Boolean {
        mutex.withLock {
            loadFromStore()
            pruneOld()
            val limit = if (tier == ModelTier.PRO) PRO_LIMIT else FLASH_LIMIT
            val list = if (tier == ModelTier.PRO) proTimestamps else flashTimestamps
            return list.size >= limit
        }
    }

    private fun pruneOld() {
        val cutoff = System.currentTimeMillis() - WINDOW_MS
        proTimestamps.removeAll { it < cutoff }
        flashTimestamps.removeAll { it < cutoff }
    }

    private suspend fun loadFromStore() {
        val prefs = OnboardingPreference.dataStore(context).data.first()
        proTimestamps.clear()
        flashTimestamps.clear()
        prefs[PRO_TIMESTAMPS_KEY]?.split(",")?.mapNotNull { it.toLongOrNull() }?.let { proTimestamps.addAll(it) }
        prefs[FLASH_TIMESTAMPS_KEY]?.split(",")?.mapNotNull { it.toLongOrNull() }?.let { flashTimestamps.addAll(it) }
    }

    private suspend fun persist() {
        OnboardingPreference.dataStore(context).edit { prefs ->
            prefs[PRO_TIMESTAMPS_KEY] = proTimestamps.joinToString(",")
            prefs[FLASH_TIMESTAMPS_KEY] = flashTimestamps.joinToString(",")
        }
    }
}
