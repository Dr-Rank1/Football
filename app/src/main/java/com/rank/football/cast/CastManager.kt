package com.rank.football.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext

enum class CastState { DISCONNECTED, CONNECTING, CONNECTED }

/** Singleton wrapper around Google Cast for streaming to external displays. */
object CastManager {
    private var castContext: CastContext? = null
    var currentDeviceName: String? = null
        private set

    /** Initializes CastContext; call from Application.onCreate. */
    fun initialize(context: Context) {
        try {
            castContext = CastContext.getSharedInstance(context)
        } catch (_: Exception) {
        }
    }

    /** Returns true when a Cast session is active. */
    fun isConnected(): Boolean = castContext?.sessionManager?.currentCastSession?.isConnected == true

    /** Loads media onto the connected Cast receiver. */
    fun loadMedia(streamUrl: String, title: String, posterUrl: String) {
        // Cast media loading handled via Cast SDK session; stub for receiver integration
    }

    /** Pauses remote Cast playback. */
    fun pauseRemote() {
        castContext?.sessionManager?.currentCastSession?.remoteMediaClient?.pause()
    }

    /** Resumes remote Cast playback. */
    fun resumeRemote() {
        castContext?.sessionManager?.currentCastSession?.remoteMediaClient?.play()
    }

    /** Stops remote Cast playback. */
    fun stopRemote() {
        castContext?.sessionManager?.currentCastSession?.remoteMediaClient?.stop()
    }

    /** Returns the CastContext if initialized. */
    fun getContext(): CastContext? = castContext
}
