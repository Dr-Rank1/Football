package com.rank.football.viewmodel

enum class StreamType { HLS, DASH, MP4 }

data class PlaybackStreamSource(
    val url: String,
    val quality: String,
    val label: String,
    val type: StreamType,
    val requiresRewardedAd: Boolean = false
)
