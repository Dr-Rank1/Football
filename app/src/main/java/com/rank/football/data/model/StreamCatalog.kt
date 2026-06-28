package com.rank.football.data.model

/** JSON payload from the remote stream catalog endpoint. */
data class StreamCatalogResponse(
    val streams: List<StreamCatalogEntry> = emptyList()
)

/** Single stream mapping entry in the remote catalog. */
data class StreamCatalogEntry(
    val fixtureId: Int,
    val title: String = "Main",
    val streamUrl: String,
    val quality: String = "HD",
    val language: String = "EN",
    val requiresRewardedAd: Boolean = false
) {
    /** Converts a catalog row into an in-app stream source. */
    fun toStreamSource(): StreamSource = StreamSource(
        fixtureId = fixtureId,
        title = title,
        streamUrl = streamUrl,
        quality = quality,
        language = language,
        requiresRewardedAd = requiresRewardedAd
    )
}
