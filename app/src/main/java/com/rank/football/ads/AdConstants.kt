package com.rank.football.ads

import com.rank.football.BuildConfig

object AdConstants {
    const val PUBLISHER_ID = "ca-app-pub-9418386170210711"
    const val ADMOB_APP_ID_TEST = "ca-app-pub-3940256099942544~3347511713"

    /** Production ad unit IDs are injected via local.properties (ADMOB_*_ID). */
    val BANNER_UNIT_ID: String get() = BuildConfig.ADMOB_BANNER_ID
    val INTERSTITIAL_UNIT_ID: String get() = BuildConfig.ADMOB_INTERSTITIAL_ID
    val REWARDED_UNIT_ID: String get() = BuildConfig.ADMOB_REWARDED_ID
    val REWARDED_INTERSTITIAL_UNIT_ID: String get() = BuildConfig.ADMOB_REWARDED_INTERSTITIAL_ID

    const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1000L
    /** Skip interstitials for this long after leaving Watch. */
    const val WATCH_AD_GRACE_MS = 3 * 60 * 1000L
}
