package com.rank.football.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val context: Context) {

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var lastShownTimestamp = 0L
    private var isOnWatchScreen = false
    private var isShowingAd = false

    companion object {
        private const val TAG = "GoalStream_AppOpen"
        private const val COOLDOWN_MS = 4 * 60 * 60 * 1000L
    }

    init {
        loadAd()
    }

    fun setOnWatchScreen(onWatch: Boolean) {
        isOnWatchScreen = onWatch
    }

    fun loadAd() {
        if (isLoading || appOpenAd != null) return
        isLoading = true
        AppOpenAd.load(
            context,
            "ca-app-pub-3940256099942544/9257395921",
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "App open ad failed: ${error.message}")
                    isLoading = false
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity) {
        val now = System.currentTimeMillis()
        if (isOnWatchScreen || isShowingAd) return
        if (now - lastShownTimestamp < COOLDOWN_MS) return
        val ad = appOpenAd ?: run { loadAd(); return }
        isShowingAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastShownTimestamp = System.currentTimeMillis()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }
        }
        ad.show(activity)
    }
}
