package com.rank.football.data.model

data class StreamSource(
    val fixtureId: Int,
    val title: String,
    val streamUrl: String,
    val quality: String,
    val language: String,
    val requiresRewardedAd: Boolean = false
)
