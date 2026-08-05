package com.rank.football.review

import android.app.Activity
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Triggers Google Play in-app review once after engagement thresholds are met. */
object InAppReviewManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Requests in-app review if user watched 3+ matches and app is 3+ days old. */
    fun maybeRequestReview(activity: Activity) {
        scope.launch {
            val context = activity.applicationContext
            if (AppPreferences.reviewShown(context).first()) return@launch
            val watchCount = AppDatabase.getInstance(context).watchHistoryDao().count()
            if (watchCount < 3) return@launch
            val installDate = AppPreferences.installDate(context).first()
            val threeDaysMs = 3L * 24 * 60 * 60 * 1000
            if (System.currentTimeMillis() - installDate < threeDaysMs) return@launch
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                    scope.launch(Dispatchers.IO) {
                        AppPreferences.setReviewShown(context)
                    }
                }
            }
        }
    }
}
