package com.rank.football.ai

import android.content.Context
import com.rank.football.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

/** Gemini client for search suggestions and lightweight AI helpers. */
object GeminiClient {

    private var rateLimiter: GeminiRateLimiter? = null

    private val flashModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 512
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE)
            )
        )
    }

    /** Initializes the rate limiter with application context. */
    fun init(context: Context) {
        rateLimiter = GeminiRateLimiter(context.applicationContext)
    }

    private suspend fun limiter(): GeminiRateLimiter =
        rateLimiter ?: throw IllegalStateException("GeminiClient.init() not called")

    /** Returns JSON search-term suggestions for a failed football search query. */
    suspend fun suggestSearchTerms(query: String): String {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || query.length < 3) return "[]"
        limiter().acquire(GeminiRateLimiter.ModelTier.FLASH)
        val prompt = """
            The user searched for '$query' in a football app.
            Suggest 3 alternative search terms they might mean.
            Return as JSON array of strings only.
        """.trimIndent()
        return runCatching {
            flashModel.generateContent(content { text(prompt) }).text.orEmpty()
        }.getOrElse { "[]" }
    }
}
