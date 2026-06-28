package com.rank.football.streaming

import android.content.Context
import android.widget.Toast
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Adjusts ExoPlayer max video bitrate based on network speed and stall history. */
class AdaptiveBitrateManager(
    private val context: Context,
    private val player: ExoPlayer,
    private val healthMonitor: StreamHealthMonitor,
    private val scope: CoroutineScope
) {
    private var lastTier = QualityTier.HD
    private var lastDowngradeToastMs = 0L

    enum class QualityTier(val maxBitrate: Int, val label: String) {
        HD(5_000_000, "HD"),
        SD(1_500_000, "SD"),
        LOW(800_000, "360p")
    }

    /** Observes health reports and applies adaptive bitrate decisions. */
    fun start() {
        scope.launch {
            healthMonitor.report
                .map { decideTier(it) }
                .distinctUntilChanged()
                .collect { tier ->
                    applyTier(tier)
                }
        }
    }

    /** Maps a health report to the preferred quality tier. */
    private fun decideTier(report: StreamHealthReport): QualityTier {
        return when {
            report.networkSpeedKbps > 5000 && report.stallCount == 0 -> QualityTier.HD
            report.networkSpeedKbps in 1500..5000 || report.stallCount <= 2 -> QualityTier.SD
            report.networkSpeedKbps < 1500 || report.stallCount > 2 -> QualityTier.LOW
            else -> QualityTier.SD
        }
    }

    /** Applies max video bitrate to ExoPlayer for the given tier. */
    private fun applyTier(tier: QualityTier) {
        try {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setMaxVideoBitrate(tier.maxBitrate)
                .build()
            if (tier.ordinal > lastTier.ordinal) {
                val now = System.currentTimeMillis()
                if (now - lastDowngradeToastMs > 30_000) {
                    lastDowngradeToastMs = now
                    Toast.makeText(
                        context,
                        "Switched to ${tier.label} — low connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            lastTier = tier
        } catch (_: Exception) {
        }
    }
}
