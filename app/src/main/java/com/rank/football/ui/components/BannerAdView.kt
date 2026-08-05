package com.rank.football.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.rank.football.ui.theme.AdBackground
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun BannerAdView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    val adViewHolder = remember { mutableStateOf<AdView?>(null) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(AdBackground),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }.also { adViewHolder.value = it }
        },
        update = { view ->
            if (adViewHolder.value !== view) adViewHolder.value = view
        }
    )

    LaunchedEffect(adUnitId) {
        var view = adViewHolder.value
        while (view == null) {
            delay(1_000)
            view = adViewHolder.value
        }
        while (true) {
            val current = adViewHolder.value ?: break
            current.loadAd(AdRequest.Builder().build())
            delay(45_000)
        }
    }
}
