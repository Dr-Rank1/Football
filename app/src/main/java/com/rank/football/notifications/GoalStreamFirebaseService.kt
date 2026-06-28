package com.rank.football.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** Handles FCM push messages for goals, match starts, and results. */
class GoalStreamFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        val fixtureId = data["fixtureId"]?.toIntOrNull() ?: return
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
