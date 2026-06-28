package com.rank.football.performance

import android.content.Context
import com.rank.football.analytics.LocalAnalytics
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

/** OkHttp EventListener that logs slow API calls to local analytics. */
class ApiPerformanceEventListener(private val context: Context) : EventListener() {
    private var callStartMs = 0L

    /** Records the start time when a call is initiated. */
    override fun callStart(call: Call) {
        callStartMs = System.currentTimeMillis()
    }

    /** Logs analytics when a call completes in over 3 seconds. */
    override fun callEnd(call: Call) {
        val duration = System.currentTimeMillis() - callStartMs
        if (duration > 3000) {
            LocalAnalytics.log(
                context,
                "slow_api_call",
                mapOf("url" to call.request().url.encodedPath, "duration_ms" to duration)
            )
        }
    }

    /** Logs failed calls that exceed the slow threshold. */
    override fun callFailed(call: Call, ioe: IOException) {
        callEnd(call)
    }

    override fun dnsStart(call: Call, domainName: String) {}
    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {}
    override fun secureConnectStart(call: Call) {}
    override fun secureConnectEnd(call: Call, handshake: Handshake?) {}
    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {}
    override fun requestHeadersStart(call: Call) {}
    override fun requestHeadersEnd(call: Call, request: Request) {}
    override fun responseHeadersStart(call: Call) {}
    override fun responseHeadersEnd(call: Call, response: okhttp3.Response) {}
    override fun responseBodyStart(call: Call) {}
    override fun responseBodyEnd(call: Call, byteCount: Long) {}
    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {}
}

/** Factory that creates a new ApiPerformanceEventListener per OkHttp call. */
class ApiPerformanceEventListenerFactory(private val context: Context) : EventListener.Factory {
    /** Returns a fresh event listener instance for the given call. */
    override fun create(call: Call): EventListener = ApiPerformanceEventListener(context.applicationContext)
}
