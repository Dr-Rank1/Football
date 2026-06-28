package com.rank.football.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/** Safe anonymous Firebase auth that never crashes when Firebase is misconfigured. */
object FirebaseAuthHelper {

    /** Returns false when google-services.json still contains placeholder credentials. */
    fun isConfigured(): Boolean {
        return try {
            val apiKey = FirebaseAuth.getInstance().app.options.apiKey.orEmpty()
            apiKey.isNotBlank() && !apiKey.contains("placeholder", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    /** Signs in anonymously and returns the UID, or empty when auth is unavailable. */
    suspend fun ensureAnonymousUser(): String {
        if (!isConfigured()) return ""
        return try {
            val auth = FirebaseAuth.getInstance()
            auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
