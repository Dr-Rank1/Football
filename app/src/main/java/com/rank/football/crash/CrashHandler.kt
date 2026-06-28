package com.rank.football.crash

import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Saves uncaught exception logs and offers crash reporting on next launch. */
class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashLog(throwable)
        } catch (_: Exception) {
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    /** Persists stack trace and device info to internal storage. */
    private fun saveCrashLog(throwable: Throwable) {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.filesDir, "crash_$ts.txt")
        val info = buildString {
            appendLine("GoalStream Crash Report")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("App: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}")
            appendLine("---")
            appendLine(throwable.stackTraceToString())
        }
        file.writeText(info)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING, file.absolutePath)
            .apply()
    }

    /** Shows email share intent if a pending crash log exists. */
    fun offerReportIfPending() {
        val path = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PENDING, null) ?: return
        val file = File(path)
        if (!file.exists()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GoalStream Crash Report")
            putExtra(Intent.EXTRA_TEXT, file.readText())
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Report crash").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_PENDING).apply()
    }

    companion object {
        private const val PREFS = "crash_handler"
        private const val KEY_PENDING = "pending_crash"

        /** Installs this handler as the default uncaught exception handler. */
        fun install(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext))
        }
    }
}
