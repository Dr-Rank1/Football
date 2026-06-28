package com.rank.football.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var lastShownTimestamp = 0L
    private var pendingNavigation: (() -> Unit)? = null

    fun loadAd() {
        InterstitialAd.load(
            context,
            AdConstants.INTERSTITIAL_TEST_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showBeforeNavigation(activity: Activity, onNavigate: () -> Unit) {
        val now = System.currentTimeMillis()
        val canShow = now - lastShownTimestamp >= AdConstants.INTERSTITIAL_COOLDOWN_MS
        val ad = interstitialAd

        if (!canShow || ad == null) {
            onNavigate()
            if (ad == null) loadAd()
            return
        }

        pendingNavigation = onNavigate
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownTimestamp = System.currentTimeMillis()
                pendingNavigation?.invoke()
                pendingNavigation = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                pendingNavigation?.invoke()
                pendingNavigation = null
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
            }
        }
        ad.show(activity)
    }
}

class RewardedAdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null

    fun loadAd() {
        RewardedAd.load(
            context,
            AdConstants.REWARDED_TEST_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun show(activity: Activity, onRewarded: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            loadAd()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadAd()
            }
        }
        ad.show(activity) {
            onRewarded()
        }
    }
}
