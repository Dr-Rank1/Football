package com.rank.football.pip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives PiP remote action broadcasts and forwards them to the registered handler. */
class PipActionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        PipActionHandler.handle(action)
    }
}

/** Holds a callback for PiP actions from the active WatchScreen session. */
object PipActionHandler {
    @Volatile
    var onRewind: (() -> Unit)? = null
    @Volatile
    var onPlayPause: (() -> Unit)? = null
    @Volatile
    var onFastForward: (() -> Unit)? = null

    /** Dispatches a PiP action string to the active player callbacks. */
    fun handle(action: String) {
        when (action) {
            PipHelper.ACTION_REWIND -> onRewind?.invoke()
            PipHelper.ACTION_PLAY_PAUSE -> onPlayPause?.invoke()
            PipHelper.ACTION_FAST_FORWARD -> onFastForward?.invoke()
        }
    }

    /** Clears all PiP callbacks when WatchScreen is disposed. */
    fun clear() {
        onRewind = null
        onPlayPause = null
        onFastForward = null
    }
}
