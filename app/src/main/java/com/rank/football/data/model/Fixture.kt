package com.rank.football.data.model

import com.google.gson.annotations.SerializedName

data class FixturesResponse(
    val response: List<FixtureItem> = emptyList(),
    val errors: Map<String, String>? = null
)

data class FixtureItem(
    val fixture: FixtureInfo,
    val league: LeagueInfo,
    val teams: TeamsInfo,
    val goals: GoalsInfo,
    val events: List<MatchEvent>? = null
)

data class FixtureInfo(
    val id: Int,
    val date: String,
    val status: FixtureStatus
)

data class FixtureStatus(
    val short: String,
    val elapsed: Int? = null,
    val long: String? = null
)

data class LeagueInfo(
    val id: Int,
    val name: String,
    val country: String? = null,
    val logo: String? = null,
    val season: Int? = null
)

data class TeamsInfo(
    val home: TeamInfo,
    val away: TeamInfo
)

data class TeamInfo(
    val id: Int? = null,
    val name: String,
    val logo: String? = null
)

data class GoalsInfo(
    val home: Int? = null,
    val away: Int? = null
)

data class MatchEvent(
    val time: EventTime,
    val team: TeamInfo,
    val player: PlayerInfo?,
    val type: String,
    val detail: String?
)

data class EventTime(
    val elapsed: Int?,
    val extra: Int? = null
)

data class PlayerInfo(
    val id: Int? = null,
    val name: String? = null
)

data class LeaguesResponse(
    val response: List<LeagueItem> = emptyList()
)

data class LeagueItem(
    val league: LeagueDetail,
    val country: CountryInfo? = null,
    val seasons: List<SeasonInfo>? = null
)

data class LeagueDetail(
    val id: Int,
    val name: String,
    val type: String? = null,
    val logo: String? = null
)

data class CountryInfo(
    val name: String? = null,
    val code: String? = null,
    val flag: String? = null
)

data class SeasonInfo(
    val year: Int,
    val current: Boolean = false
)

data class StandingsResponse(
    val response: List<StandingsLeagueItem> = emptyList()
)

data class StandingsLeagueItem(
    val league: StandingsLeague
)

data class StandingsLeague(
    val id: Int,
    val name: String,
    val season: Int,
    val standings: List<List<StandingEntry>>
)

data class StandingEntry(
    val rank: Int,
    val team: TeamInfo,
    val points: Int,
    val goalsDiff: Int,
    val form: String? = null,
    val all: StandingStats
)

data class StandingStats(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: StandingGoals? = null
)

data class StandingGoals(
    val `for`: Int = 0,
    val against: Int = 0
)

fun FixtureItem.isLive(): Boolean {
    val status = fixture.status.short
    return status in listOf("1H", "2H", "HT", "ET", "BT", "P", "LIVE", "INT")
}

fun FixtureItem.isFinished(): Boolean = fixture.status.short == "FT"

fun FixtureItem.isUpcoming(): Boolean = fixture.status.short == "NS"

fun FixtureItem.displayScore(): String {
    val home = goals.home?.toString() ?: "-"
    val away = goals.away?.toString() ?: "-"
    return "$home - $away"
}

fun FixtureItem.kickOffTime(): String {
    return try {
        val timePart = fixture.date.substringAfter("T").take(5)
        timePart
    } catch (_: Exception) {
        "--:--"
    }
}
