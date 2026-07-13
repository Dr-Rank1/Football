package com.rank.football.data.repository

import com.rank.football.BuildConfig
import com.rank.football.data.api.RetrofitClient
import com.rank.football.data.firebase.FirebaseAuthHelper
import com.rank.football.data.model.StreamCatalogResponse
import com.rank.football.data.model.StreamSource
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Request

/** Fetches fixture-to-stream mappings from remote URL and Firebase Realtime Database. */
class StreamCatalogRepository {

    private val httpClient = RetrofitClient.createOkHttpClient()
    private val gson = Gson()

    /** Downloads stream mappings from all configured sources into the shared repository. */
    suspend fun syncInto(streamRepository: StreamRepository): Boolean = withContext(Dispatchers.IO) {
        val merged = linkedMapOf<Int, List<StreamSource>>()
        loadFromUrl()?.let { merged.putAll(it) }
        loadFromFirebase()?.let { firebase ->
            firebase.forEach { (id, sources) -> merged[id] = sources }
        }
        if (merged.isEmpty()) {
            streamRepository.markSynced(false)
            return@withContext false
        }
        streamRepository.replaceAll(merged)
        true
    }

    /** Loads stream entries from the optional HTTP catalog URL. */
    private fun loadFromUrl(): Map<Int, List<StreamSource>>? {
        val url = BuildConfig.STREAM_CATALOG_URL.trim()
        if (url.isBlank()) return null
        return runCatching {
            val response = httpClient.newCall(Request.Builder().url(url).get().build()).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null
            val catalog = gson.fromJson(body, StreamCatalogResponse::class.java)
            catalog.streams
                .filter { it.fixtureId > 0 && it.streamUrl.isNotBlank() }
                .groupBy { it.fixtureId }
                .mapValues { (_, entries) -> entries.map { it.toStreamSource() } }
        }.getOrNull()
    }

    /** Loads stream entries from Firebase Realtime Database at `/streams/{fixtureId}`. */
    private suspend fun loadFromFirebase(): Map<Int, List<StreamSource>>? {
        if (!FirebaseAuthHelper.isConfigured()) return null
        return runCatching {
            val snapshot = FirebaseDatabase.getInstance().reference.child("streams").get().await()
            if (!snapshot.exists()) return null
            buildMap {
                snapshot.children.forEach { child ->
                    val fixtureId = child.key?.toIntOrNull() ?: return@forEach
                    val url = child.child("streamUrl").getValue(String::class.java)
                        ?: child.getValue(String::class.java)
                    if (url.isNullOrBlank()) return@forEach
                    val title = child.child("title").getValue(String::class.java) ?: "Main"
                    val quality = child.child("quality").getValue(String::class.java) ?: "HD"
                    val language = child.child("language").getValue(String::class.java) ?: "EN"
                    put(
                        fixtureId,
                        listOf(
                            StreamSource(
                                fixtureId = fixtureId,
                                title = title,
                                streamUrl = url,
                                quality = quality,
                                language = language
                            )
                        )
                    )
                }
            }.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }
}
