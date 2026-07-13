package com.rank.football.data.local

import com.rank.football.data.model.StandingEntry

/** Maps an API standings row into a Room cache entity with real GF/GA and form. */
fun StandingEntry.toCachedStanding(leagueId: Int, nowMs: Long = System.currentTimeMillis()): CachedStanding =
    CachedStanding(
        teamId = team.id ?: 0,
        leagueId = leagueId,
        position = rank,
        teamName = team.name,
        teamLogo = team.logo ?: "",
        played = all.played,
        won = all.win,
        drawn = all.draw,
        lost = all.lose,
        goalsFor = all.goals?.`for` ?: 0,
        goalsAgainst = all.goals?.against ?: 0,
        points = points,
        form = form.orEmpty(),
        lastUpdated = nowMs
    )
