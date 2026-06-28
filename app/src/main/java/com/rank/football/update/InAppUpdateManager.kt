package com.rank.football.update

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/** Checks Play Store for app updates and triggers flexible or immediate flows. */
object InAppUpdateManager {

    /** Checks for updates and starts FLEXIBLE flow for minor or IMMEDIATE for major version bumps. */
    fun checkForUpdate(activity: Activity, onFlexibleDownloaded: () -> Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener
            val currentMajor = com.rank.football.BuildConfig.VERSION_NAME.substringBefore('.').toIntOrNull() ?: 0
            val availableMajor = info.availableVersionCode() / 1000
            val updateType = if (availableMajor > currentMajor) {
                AppUpdateType.IMMEDIATE
            } else {
                AppUpdateType.FLEXIBLE
            }
            if (!info.isUpdateTypeAllowed(updateType)) return@addOnSuccessListener
            appUpdateManager.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.newBuilder(updateType).build(),
                UPDATE_REQUEST_CODE
            )
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == com.google.android.play.core.install.model.InstallStatus.DOWNLOADED) {
                onFlexibleDownloaded()
            }
        }
    }

    /** Completes a flexible update after user confirms restart. */
    fun completeFlexibleUpdate(activity: Activity) {
        AppUpdateManagerFactory.create(activity).completeUpdate()
    }

    const val UPDATE_REQUEST_CODE = 9001
}
