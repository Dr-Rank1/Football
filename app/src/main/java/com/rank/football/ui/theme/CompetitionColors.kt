package com.rank.football.ui.theme

import androidx.compose.ui.graphics.Color

/** Signature left-border colours for major competitions (UI reference). */
object CompetitionColors {
    private val byName = mapOf(
        "premier league" to Color(0xFF7B2FBE),
        "la liga" to Color(0xFFE63946),
        "bundesliga" to Color(0xFFD3010C),
        "serie a" to Color(0xFF1E40AF),
        "ligue 1" to Color(0xFF1D4ED8),
        "uefa champions league" to Color(0xFF1E3A8A),
        "champions league" to Color(0xFF1E3A8A),
        "uefa cl" to Color(0xFF1E3A8A),
        "uefa europa league" to Color(0xFFFF6B00),
        "europa league" to Color(0xFFFF6B00),
        "mls" to Color(0xFF13294B),
        "eredivisie" to Color(0xFFD42027),
        "primeira liga" to Color(0xFF006600),
        "saudi pro league" to Color(0xFF00A651),
        "world cup" to Color(0xFF9747FF)
    )

    fun accent(leagueName: String?, leagueId: Int = 0): Color {
        val key = leagueName?.lowercase()?.trim().orEmpty()
        byName[key]?.let { return it }
        byName.entries.firstOrNull { key.contains(it.key) }?.value?.let { return it }
        // Stable fallback from league id so unknown comps still get a colour
        val palette = listOf(
            Color(0xFF7B2FBE), Color(0xFFE63946), Color(0xFF1E3A8A),
            Color(0xFFD3010C), Color(0xFF1D4ED8), PitchGreen
        )
        return palette[leagueId.coerceAtLeast(0) % palette.size]
    }
}
