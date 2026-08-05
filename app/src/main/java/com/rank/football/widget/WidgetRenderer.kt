package com.rank.football.widget



import android.app.PendingIntent

import android.appwidget.AppWidgetManager

import android.content.Context

import android.content.Intent

import android.widget.RemoteViews

import com.rank.football.MainActivity

import com.rank.football.R

import com.rank.football.data.model.FixtureItem

import com.rank.football.data.model.isLive

import com.rank.football.widget.WidgetRefreshReceiver.Companion.ACTION_REFRESH

import kotlinx.coroutines.flow.first

import kotlinx.coroutines.runBlocking



/** Builds RemoteViews for GoalStream live score widgets. */

object WidgetRenderer {



    /** Picks layout and populates views based on widget cell size. */

    fun render(

        context: Context,

        appWidgetId: Int,

        minWidth: Int,

        minHeight: Int,

        liveFixtures: List<FixtureItem>,

        upcoming: FixtureItem? = null

    ): RemoteViews {

        val prefs = readPrefs(context)

        val filteredLive = filterByLeaguePreference(liveFixtures, prefs.leagueFilter)

        val layout = when {

            minWidth >= 280 && minHeight >= 280 -> R.layout.widget_scoreboard_large

            minWidth < 180 || minHeight < 110 -> R.layout.widget_compact

            else -> R.layout.widget_live_score

        }

        val views = RemoteViews(context.packageName, layout)

        applyTheme(context, views, prefs.theme)

        attachRefreshIntent(context, appWidgetId, views)

        when (layout) {

            R.layout.widget_compact -> bindCompact(context, views, filteredLive.firstOrNull())

            R.layout.widget_scoreboard_large -> bindLarge(context, views, filteredLive, upcoming, prefs.maxMatches)

            else -> bindMedium(context, views, filteredLive)

        }

        attachOpenIntent(context, views, filteredLive.firstOrNull()?.fixture?.id ?: 0)

        return views

    }



    private data class WidgetPrefs(val theme: String, val maxMatches: Int, val leagueFilter: String?)



    /** Reads widget theme and display limits from DataStore. */

    private fun readPrefs(context: Context): WidgetPrefs = runBlocking {

        WidgetPrefs(

            theme = WidgetPreferenceStore.theme(context).first(),

            maxMatches = WidgetPreferenceStore.maxMatches(context).first(),

            leagueFilter = WidgetPreferenceStore.leagueFilter(context).first().takeIf { it.isNotBlank() }

        )

    }



    /** Keeps only fixtures from the preferred league when a filter is set. */

    private fun filterByLeaguePreference(

        fixtures: List<FixtureItem>,

        leagueFilter: String?

    ): List<FixtureItem> {

        if (leagueFilter.isNullOrBlank()) return fixtures

        return fixtures.filter {

            it.league.name.contains(leagueFilter, ignoreCase = true) ||

                it.league.country?.contains(leagueFilter, ignoreCase = true) == true

        }.ifEmpty { fixtures }

    }



    /** Applies accent colors for the selected widget theme. */
    private fun applyTheme(context: Context, views: RemoteViews, theme: String) {
        val accentRes = when (theme) {
            "pitch_light" -> R.color.pitch_green
            "classic_white" -> android.R.color.white
            else -> R.color.pitch_green
        }
        val accent = context.getColor(accentRes)
        listOf(R.id.widget_header, R.id.widget_league1, R.id.widget_league2, R.id.widget_refresh).forEach { id ->
            runCatching { views.setTextColor(id, accent) }
        }
    }



    /** Binds the small 2×2 single-match widget. */

    private fun bindCompact(context: Context, views: RemoteViews, match: FixtureItem?) {

        if (match == null) {

            views.setTextViewText(R.id.widget_league, context.getString(R.string.app_name))

            views.setTextViewText(R.id.widget_score, "No live matches")

            views.setTextViewText(R.id.widget_minute, "")

            return

        }

        views.setTextViewText(R.id.widget_league, match.league.name)

        views.setTextViewText(

            R.id.widget_score,

            "${match.teams.home.name} ${match.goals.home ?: 0} - ${match.goals.away ?: 0} ${match.teams.away.name}"

        )

        val minute = if (match.isLive()) "${match.fixture.status.elapsed ?: 0}' LIVE" else match.fixture.status.short

        views.setTextViewText(R.id.widget_minute, minute)

    }



    /** Binds the medium 4×2 dual-match widget. */

    private fun bindMedium(context: Context, views: RemoteViews, live: List<FixtureItem>) {

        views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_live_header))

        views.setTextViewText(R.id.widget_match1, formatRow(live.getOrNull(0)))

        views.setTextViewText(R.id.widget_match2, formatRow(live.getOrNull(1)))

        views.setTextViewText(R.id.widget_cta, "Tap to watch")

    }



    /** Binds the large 4×4 scoreboard grouped by league. */

    private fun bindLarge(

        context: Context,

        views: RemoteViews,

        live: List<FixtureItem>,

        upcoming: FixtureItem?,

        maxMatches: Int

    ) {

        views.setTextViewText(R.id.widget_header, context.getString(R.string.widget_live_header))

        val capped = live.take(maxMatches)

        val grouped = capped.groupBy { it.league.name }

        val leagues = grouped.keys.take(2)

        leagues.getOrNull(0)?.let { league ->

            views.setTextViewText(R.id.widget_league1, league.uppercase())

            val rows = grouped[league].orEmpty()

            views.setTextViewText(R.id.widget_row1, formatRow(rows.getOrNull(0)))

            views.setTextViewText(R.id.widget_row2, formatRow(rows.getOrNull(1)))

        }

        leagues.getOrNull(1)?.let { league ->

            views.setTextViewText(R.id.widget_league2, league.uppercase())

            val rows = grouped[league].orEmpty()

            views.setTextViewText(R.id.widget_row3, formatRow(rows.getOrNull(0)))

            views.setTextViewText(R.id.widget_row4, formatRow(rows.getOrNull(1)))

        }

        upcoming?.let {

            views.setTextViewText(

                R.id.widget_next,

                "NEXT: ${it.teams.home.name} vs ${it.teams.away.name}"

            )

        }

    }



    /** Formats a fixture as a single widget row string. */

    private fun formatRow(match: FixtureItem?): String {

        match ?: return "—"

        val minute = if (match.isLive()) "${match.fixture.status.elapsed ?: 0}' LIVE" else match.fixture.status.short

        return "${match.teams.home.name} ${match.goals.home ?: 0}-${match.goals.away ?: 0} ${match.teams.away.name}  $minute"

    }



    /** Attaches a tap intent that opens WatchScreen for the given fixture. */

    private fun attachOpenIntent(context: Context, views: RemoteViews, fixtureId: Int) {

        if (fixtureId <= 0) return

        val intent = Intent(context, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("fixtureId", fixtureId)

        }

        val pending = PendingIntent.getActivity(

            context, fixtureId, intent,

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )

        listOf(R.id.widget_cta, R.id.widget_score, R.id.widget_row1).forEach { id ->

            runCatching { views.setOnClickPendingIntent(id, pending) }

        }

    }



    /** Attaches refresh button to trigger immediate widget update. */

    private fun attachRefreshIntent(context: Context, appWidgetId: Int, views: RemoteViews) {

        val intent = Intent(context, WidgetRefreshReceiver::class.java).apply {

            action = ACTION_REFRESH

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        }

        val pending = PendingIntent.getBroadcast(

            context, appWidgetId, intent,

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )

        runCatching { views.setOnClickPendingIntent(R.id.widget_refresh, pending) }

    }

}

