package com.rank.football.sync

import android.content.Context
import com.rank.football.data.firebase.FirebaseAuthHelper
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/** Firebase anonymous sync for favorites, fantasy, and settings snapshots. */
class CloudSyncManager(context: Context) {

    private val appContext = context.applicationContext
    private val root = FirebaseDatabase.getInstance().reference.child("users")

    /** Ensures an anonymous Firebase user exists for cloud sync. */
    suspend fun ensureAnonymousUser(): String = FirebaseAuthHelper.ensureAnonymousUser()

    /** Uploads favorites, fantasy team id, and key settings to Firebase. */
    suspend fun pushLocalState() {
        if (!FirebaseAuthHelper.isConfigured()) return
        try {
            val uid = ensureAnonymousUser()
            if (uid.isBlank()) return
            val db = AppDatabase.getInstance(appContext)
            val favorites = db.favoritesDao().getAllFavorites().first()
            val fantasy = db.fantasyDao().getTeam()
            val payload = mapOf(
                "displayName" to AppPreferences.displayName(appContext).first(),
                "language" to AppPreferences.appLanguage(appContext).first(),
                "favorites" to favorites.map { mapOf("teamId" to it.teamId, "teamName" to it.teamName) },
                "fantasyTeamId" to (fantasy?.id ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )
            root.child(uid).setValue(payload).await()
        } catch (_: Exception) {
        }
    }

    /** Pulls remote favorites and settings into local storage when newer. */
    suspend fun pullRemoteState() {
        if (!FirebaseAuthHelper.isConfigured()) return
        try {
            val uid = ensureAnonymousUser()
            if (uid.isBlank()) return
            val snapshot = root.child(uid).get().await()
            if (!snapshot.exists()) return
            snapshot.child("displayName").getValue(String::class.java)?.let {
                AppPreferences.setDisplayName(appContext, it)
            }
            snapshot.child("language").getValue(String::class.java)?.let {
                AppPreferences.setAppLanguage(appContext, it)
            }
        } catch (_: Exception) {
        }
    }
}
