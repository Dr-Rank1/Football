package com.rank.football

import android.app.Application
import coil.ImageLoader
import android.os.StrictMode
import android.os.SystemClock
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.rank.football.analytics.LocalAnalytics
import com.rank.football.performance.AppPerformanceMonitor
import com.rank.football.cast.CastManager
import com.rank.football.crash.CrashHandler
import com.rank.football.data.firebase.ChatRepository
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.AppPreferences
import com.rank.football.notifications.NotificationHelper
import com.rank.football.ai.GeminiClient
import com.rank.football.performance.DatabaseOptimizer
import com.rank.football.performance.ImagePipelineOptimizer
import com.rank.football.sync.CloudSyncManager
import com.rank.football.security.DeviceSecurity
import com.rank.football.data.repository.StreamCatalogRepository
import com.rank.football.data.repository.StreamRepository
import com.rank.football.workers.DataSyncWorker
import com.rank.football.workers.FavoriteTeamTrackerWorker
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GoalStreamApp : Application() {

    lateinit var imageLoader: ImageLoader
        private set

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val streamRepository: StreamRepository by lazy { StreamRepository() }
    private val streamCatalogRepository by lazy { StreamCatalogRepository() }
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }
    )

    override fun onCreate() {
        super.onCreate()
        val startupMs = SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
        }
        CrashHandler.install(this)
        GeminiClient.init(this)
        imageLoader = ImagePipelineOptimizer.createOptimizedLoader(this)

        FirebaseApp.initializeApp(this)
        NotificationHelper.createNotificationChannels(this)
        CastManager.initialize(this)
        LocalAnalytics.log(this, "app_startup", mapOf("duration_ms" to startupMs))
        DeviceSecurity.rootWarning()?.let {
            LocalAnalytics.log(this, "root_detected", emptyMap())
        }
        AppPerformanceMonitor.logMemoryUsage(this)

        appScope.launch {
            AppPreferences.ensureInstallDate(this@GoalStreamApp)
            val lang = AppPreferences.appLanguage(this@GoalStreamApp).first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
            ChatRepository(this@GoalStreamApp).ensureAuth()
            CloudSyncManager(this@GoalStreamApp).pullRemoteState()
            DatabaseOptimizer.optimize(this@GoalStreamApp)
            syncStreamCatalog()
            DataSyncWorker.schedule(this@GoalStreamApp)
            FavoriteTeamTrackerWorker.schedule(this@GoalStreamApp)
        }

        appScope.launch {
            val requestConfig = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .build()
            MobileAds.setRequestConfiguration(requestConfig)
            MobileAds.initialize(this@GoalStreamApp) {}
        }
    }

    /** Refreshes stream mappings from the remote catalog endpoint. */
    suspend fun syncStreamCatalog(): Boolean =
        streamCatalogRepository.syncInto(streamRepository)
}
