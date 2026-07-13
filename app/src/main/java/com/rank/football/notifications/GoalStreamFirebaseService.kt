package com.rank.football.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.rank.football.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Handles FCM push messages for goals, match starts, and results. */
class GoalStreamFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        val fixtureId = data["fixtureId"]?.toIntOrNull() ?: return

        val allow = runBlocking {
            when (type) {
                "goal" -> {
                    if (!AppPreferences.goalAlerts(this@GoalStreamFirebaseService).first()) return@runBlocking false
                    if (!AppPreferences.favoriteOnlyNotifications(this@GoalStreamFirebaseService).first()) {
                        return@runBlocking true
                    }
                    val homeId = data["homeTeamId"]?.toIntOrNull()
                    val awayId = data["awayTeamId"]?.toIntOrNull()
                    val favs = FavoritesRepository(AppDatabase.getInstance(this@GoalStreamFirebaseService))
                        .allFavorites.first().map { it.teamId }.toSet()
                    homeId in favs || awayId in favs
                }
                "starting", "result" -> AppPreferences.matchReminders(this@GoalStreamFirebaseService).first()
                else -> true
            }
        }
        if (!allow) return

        when (type) {
            "goal" -> NotificationHelper.showGoalNotification(
                this,
                fixtureId,
                "⚽ ${data["scorerName"]} — ${data["score"]} (${data["minute"]}')"
            )
            "starting" -> NotificationHelper.showMatchStartNotification(
                this,
                fixtureId,
                "🔴 ${data["homeTeam"]} vs ${data["awayTeam"]}"
            )
            "result" -> NotificationHelper.showResultNotification(
                this,
                fixtureId,
                "FT: ${data["homeTeam"]} ${data["score"]} ${data["awayTeam"]}"
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d("GoalStream_FCM", "New token: $token")
    }
}
