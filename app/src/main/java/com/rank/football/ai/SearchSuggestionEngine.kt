package com.rank.football.ai

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Suggests alternative football search terms when API returns no results. */
class SearchSuggestionEngine {
    private val gson = Gson()

    /** Returns up to 3 AI-suggested alternative search terms for a query. */
    suspend fun suggest(query: String): List<String> {
        val raw = GeminiClient.suggestSearchTerms(query)
        if (raw.isBlank()) return emptyList()
        return try {
            val text = raw.replace("```json", "").replace("```", "").trim()
            gson.fromJson<List<String>>(text, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
