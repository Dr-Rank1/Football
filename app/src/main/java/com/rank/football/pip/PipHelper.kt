package com.rank.football.pip

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.rank.football.R

/** Builds Picture-in-Picture RemoteActions for rewind, play/pause, and fast-forward. */
object PipHelper {
    const val ACTION_REWIND = "com.rank.football.pip.REWIND"
    const val ACTION_PLAY_PAUSE = "com.rank.football.pip.PLAY_PAUSE"
    const val ACTION_FAST_FORWARD = "com.rank.football.pip.FAST_FORWARD"

    /** Creates the three interactive PiP remote actions. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun buildPipActions(context: android.content.Context, isPlaying: Boolean): List<RemoteAction> {
        return listOf(
            remoteAction(context, ACTION_REWIND, R.string.pip_rewind, android.R.drawable.ic_media_rew),
            remoteAction(context, ACTION_PLAY_PAUSE, if (isPlaying) R.string.pip_pause else R.string.pip_play,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
            remoteAction(context, ACTION_FAST_FORWARD, R.string.pip_forward, android.R.drawable.ic_media_ff)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun remoteAction(context: android.content.Context, action: String, titleRes: Int, iconRes: Int): RemoteAction {
        val intent = Intent(action).setPackage(context.packageName)
        val pending = PendingIntent.getBroadcast(
            context, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return RemoteAction(
            Icon.createWithResource(context, iconRes),
            context.getString(titleRes),
            context.getString(titleRes),
            pending
        )
    }
}
