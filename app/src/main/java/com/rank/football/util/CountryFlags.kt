package com.rank.football.util

object CountryFlags {
    private val flags = mapOf(
        "England" to "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        "Spain" to "🇪🇸",
        "Italy" to "🇮🇹",
        "Germany" to "🇩🇪",
        "France" to "🇫🇷",
        "Portugal" to "🇵🇹",
        "Netherlands" to "🇳🇱",
        "Belgium" to "🇧🇪",
        "Kenya" to "🇰🇪",
        "World" to "🌍",
        "Europe" to "🇪🇺",
        "Brazil" to "🇧🇷",
        "Argentina" to "🇦🇷",
        "USA" to "🇺🇸",
        "Turkey" to "🇹🇷",
        "Scotland" to "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
        "Wales" to "🏴󠁧󠁢󠁷󠁬󠁳󠁿"
    )

    fun flagFor(country: String?): String {
        if (country.isNullOrBlank()) return ""
        return flags[country] ?: "🏳️"
    }
}
