package com.rank.football.streaming

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FilterInputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Local NanoHTTPD proxy that forwards stream requests with custom Referer headers. */
class StreamProxyServer(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : NanoHTTPD(0) {

    @Volatile
    private var boundPort: Int = -1

    /** Starts the proxy on an ephemeral localhost port. */
    fun startServer() {
        try {
            if (!isAlive) {
                start(SOCKET_READ_TIMEOUT, false)
                boundPort = listeningPort
            }
        } catch (e: Exception) {
            boundPort = -1
            Log.e(TAG, "Failed to start stream proxy", e)
        }
    }

    /** Stops the proxy server and releases resources. */
    fun stopServer() {
        try {
            stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop stream proxy", e)
        }
        boundPort = -1
        try {
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to shut down proxy client", e)
        }
    }

    /**
     * Builds a localhost proxy URL for [originalUrl], or returns the original
     * URL when the proxy is not listening.
     */
    fun buildProxyUrl(originalUrl: String): String {
        val port = boundPort
        if (!isAlive || port <= 0) return originalUrl
        val encoded = URLEncoder.encode(originalUrl, Charsets.UTF_8.name())
        return "http://127.0.0.1:$port/stream?url=$encoded"
    }

    override fun serve(session: IHTTPSession): Response {
        val urlParam = session.parameters["url"]?.firstOrNull()
            ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Missing url")
        val targetUrl = URLDecoder.decode(urlParam, Charsets.UTF_8.name())
        return try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("Referer", "https://goalstream.app/")
                .header("Origin", "https://goalstream.app")
                .header("User-Agent", "GoalStream/4.0 Android")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body ?: run {
                response.close()
                return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Empty body"
                )
            }
            val mime = response.header("Content-Type") ?: "application/octet-stream"
            val stream = object : FilterInputStream(body.byteStream()) {
                override fun close() {
                    try {
                        super.close()
                    } finally {
                        response.close()
                    }
                }
            }
            newChunkedResponse(
                Response.Status.lookup(response.code) ?: Response.Status.INTERNAL_ERROR,
                mime,
                stream
            )
        } catch (e: Exception) {
            Log.e(TAG, "Proxy fetch failed: $targetUrl", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, e.message ?: "Error")
        }
    }

    companion object {
        private const val TAG = "StreamProxy"
        private const val SOCKET_READ_TIMEOUT = 30_000
    }
}
