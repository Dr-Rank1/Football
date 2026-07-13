package com.rank.football.cast

import android.content.Context
import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage

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

    /**
     * Registers a listener that fires when a Cast session starts or resumes.
     * Caller must [detachSessionListener] in onDispose.
     */
    fun attachSessionListener(onReady: () -> Unit): SessionManagerListener<Session>? {
        val manager = castContext?.sessionManager ?: return null
        val listener = object : SessionManagerListener<Session> {
            override fun onSessionStarting(session: Session) = Unit
            override fun onSessionStarted(session: Session, sessionId: String) = onReady()
            override fun onSessionStartFailed(session: Session, error: Int) = Unit
            override fun onSessionEnding(session: Session) = Unit
            override fun onSessionEnded(session: Session, error: Int) {
                currentDeviceName = null
            }
            override fun onSessionResuming(session: Session, sessionId: String) = Unit
            override fun onSessionResumed(session: Session, wasSuspended: Boolean) = onReady()
            override fun onSessionResumeFailed(session: Session, error: Int) = Unit
            override fun onSessionSuspended(session: Session, reason: Int) = Unit
        }
        manager.addSessionManagerListener(listener)
        return listener
    }

    /** Removes a previously attached session listener. */
    fun detachSessionListener(listener: SessionManagerListener<Session>?) {
        if (listener == null) return
        castContext?.sessionManager?.removeSessionManagerListener(listener)
    }

    /** Loads media onto the connected Cast receiver. */
    fun loadMedia(
        streamUrl: String,
        title: String,
        posterUrl: String = "",
        isLive: Boolean = false
    ) {
        val session = castContext?.sessionManager?.currentCastSession as? CastSession ?: return
        if (!session.isConnected) return
        val client = session.remoteMediaClient ?: return
        currentDeviceName = session.castDevice?.friendlyName
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, title)
            if (posterUrl.isNotBlank()) {
                addImage(WebImage(Uri.parse(posterUrl)))
            }
        }
        val contentType = when {
            streamUrl.contains(".m3u8", ignoreCase = true) -> "application/x-mpegURL"
            streamUrl.contains(".mpd", ignoreCase = true) -> "application/dash+xml"
            else -> "video/mp4"
        }
        val streamType = if (isLive) MediaInfo.STREAM_TYPE_LIVE else MediaInfo.STREAM_TYPE_BUFFERED
        val mediaInfo = MediaInfo.Builder(streamUrl)
            .setStreamType(streamType)
            .setContentType(contentType)
            .setMetadata(metadata)
            .build()
        client.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()
        )
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
