@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.rank.football.streaming

import android.content.Context
import android.net.TrafficStats
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rank.football.analytics.LocalAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Tracks playback buffer, network speed, and stall metrics during streaming. */
data class StreamHealthReport(
    val bufferPercent: Int = 0,
    val networkSpeedKbps: Long = 0,
    val droppedFrames: Int = 0,
    val currentBitrateKbps: Long = 0,
    val stallCount: Int = 0,
    val stallDurationMs: Long = 0
)

/** Collects ExoPlayer and network metrics every 5 seconds during playback. */
class StreamHealthMonitor(
    private val context: Context,
    private val player: ExoPlayer,
    private val scope: CoroutineScope
) {
    private val _report = MutableStateFlow(StreamHealthReport())
    val report: StateFlow<StreamHealthReport> = _report.asStateFlow()

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastSampleTime = System.currentTimeMillis()
    private var stallCount = 0
    private var stallDurationMs = 0L
    private var stallStartMs = 0L
    private var analyticsCounter = 0
    private var sampleJob: Job? = null

    /** Starts the background health sampling loop. */
    fun start() {
        if (sampleJob?.isActive == true) return
        sampleJob = scope.launch {
            while (isActive) {
                sample()
                delay(5_000)
            }
        }
    }

    /** Cancels health sampling so it cannot touch a released player. */
    fun stop() {
        sampleJob?.cancel()
        sampleJob = null
    }

    /** Samples current player and network state into a health report. */
    private fun sample() {
        try {
            sampleUnsafe()
        } catch (_: Exception) {
        }
    }

    private fun sampleUnsafe() {
        val now = System.currentTimeMillis()
        val rx = TrafficStats.getTotalRxBytes()
        val elapsedSec = ((now - lastSampleTime).coerceAtLeast(1)) / 1000.0
        val speedKbps = if (rx >= lastRxBytes && elapsedSec > 0) {
            ((rx - lastRxBytes) * 8 / elapsedSec / 1000).toLong()
        } else 0L
        lastRxBytes = rx
        lastSampleTime = now

        when (player.playbackState) {
            Player.STATE_BUFFERING -> {
                if (stallStartMs == 0L) stallStartMs = now
            }
            Player.STATE_READY, Player.STATE_ENDED -> {
                if (stallStartMs > 0L) {
                    stallCount++
                    stallDurationMs += now - stallStartMs
                    stallStartMs = 0L
                }
            }
            Player.STATE_IDLE -> Unit
        }

        val bitrate = try {
            player.videoFormat?.bitrate?.toLong()?.div(1000) ?: 0L
        } catch (_: Exception) {
            0L
        }

        _report.value = StreamHealthReport(
            bufferPercent = player.bufferedPercentage,
            networkSpeedKbps = speedKbps,
            droppedFrames = 0,
            currentBitrateKbps = bitrate,
            stallCount = stallCount,
            stallDurationMs = stallDurationMs
        )

        analyticsCounter++
        if (analyticsCounter >= 6) {
            analyticsCounter = 0
            LocalAnalytics.log(context, "stream_health", mapOf(
                "buffer" to _report.value.bufferPercent,
                "speedKbps" to _report.value.networkSpeedKbps,
                "stalls" to stallCount
            ))
        }
    }
}
