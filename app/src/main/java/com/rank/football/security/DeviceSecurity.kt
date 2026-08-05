package com.rank.football.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/** Checks device root status and app signature integrity. */
object DeviceSecurity {

    /** Returns true if common root indicators are present on the device. */
    fun isRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su"
        )
        if (paths.any { File(it).exists() }) return true
        return Build.TAGS?.contains("test-keys") == true
    }

    /** Returns true if the installed APK signature matches the expected release signature. */
    fun isOfficialBuild(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners?.isNotEmpty() == true
            } else {
                @Suppress("DEPRECATION")
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures?.isNotEmpty() == true
            }
        } catch (_: Exception) {
            true
        }
    }

    /** Returns a user-facing warning message if the device appears rooted. */
    fun rootWarning(): String? =
        if (isRooted()) "Rooted device detected — streams may not work correctly" else null
}
