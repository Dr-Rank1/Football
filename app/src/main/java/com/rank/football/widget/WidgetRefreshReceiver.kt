package com.rank.football.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rank.football.workers.WidgetUpdateWorker

/** Handles widget refresh button taps from RemoteViews. */
class WidgetRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_REFRESH) return
        WidgetUpdateWorker.enqueue(context)
    }

    companion object {
        /** Intent action for manual widget refresh. */
        const val ACTION_REFRESH = "com.rank.football.widget.REFRESH"
    }
}
