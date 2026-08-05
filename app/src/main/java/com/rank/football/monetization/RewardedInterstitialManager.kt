package com.rank.football.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.rank.football.ads.AdConstants

/** Manages preloaded rewarded interstitial ads for premium content unlocks. */
class RewardedInterstitialManager(private val context: Context) {

    private var ad: RewardedInterstitialAd? = null
    var isLoaded: Boolean = false
        private set

    /** Preloads a rewarded interstitial ad from AdMob. */
    fun loadAd() {
        RewardedInterstitialAd.load(
            context,
            AdConstants.REWARDED_INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: RewardedInterstitialAd) {
                    ad = loaded
                    isLoaded = true
                    AdRevenueTracker.logImpression(context, AdRevenueTracker.AdType.REWARDED_INTERSTITIAL)
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoaded = false
                    ad = null
                }
            }
        )
    }

    /** Shows the ad and grants reward coins on completion. */
    fun show(activity: Activity, onReward: (Int) -> Unit, onDismiss: () -> Unit = {}) {
        val current = ad
        if (current == null) {
            loadAd()
            onDismiss()
            return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                isLoaded = false
                loadAd()
                onDismiss()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad = null
                isLoaded = false
                loadAd()
                onDismiss()
            }
        }
        current.show(activity) { reward ->
            onReward(100)
        }
    }
}
