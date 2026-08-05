package com.rank.football.streaming

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Local NanoHTTPD proxy that forwards stream requests with custom Referer headers. */
class StreamProxyServer(
    private val port: Int = DEFAULT_PORT,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : NanoHTTPD(port) {

    /** Starts the proxy server on the configured port. */
    fun startServer() {
        try {
            if (!isAlive) start(SOCKET_READ_TIMEOUT, false)
        } catch (e: Exception) {
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
        try {
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to shut down proxy client", e)
        }
    }

    /** Builds a localhost proxy URL for the given remote stream URL. */
    fun buildProxyUrl(originalUrl: String): String {
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
            newChunkedResponse(
                Response.Status.lookup(response.code),
                mime,
                body.byteStream()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Proxy fetch failed: $targetUrl", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, e.message ?: "Error")
        }
    }

    companion object {
        private const val TAG = "StreamProxy"
        const val DEFAULT_PORT = 8888
        private const val SOCKET_READ_TIMEOUT = 30_000
    }
}
