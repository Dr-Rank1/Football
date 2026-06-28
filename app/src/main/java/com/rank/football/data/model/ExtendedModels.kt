package com.rank.football.data.model

data class EventsResponse(
    val response: List<FixtureEventItem> = emptyList()
)

data class FixtureEventItem(
    val time: EventTime,
    val team: TeamInfo,
    val player: PlayerInfo?,
    val type: String,
    val detail: String?
)

data class StatisticsResponse(
    val response: List<FixtureStatisticsItem> = emptyList()
)

data class FixtureStatisticsItem(
    val team: TeamInfo,
    val statistics: List<StatItem>
)

data class StatItem(
    val type: String,
    val value: Any?
)

fun StatItem.intValue(): Int {
    return when (val v = value) {
        is Number -> v.toInt()
        is String -> v.replace("%", "").toIntOrNull() ?: 0
        else -> 0
    }
}

data class LineupsResponse(
    val response: List<LineupItem> = emptyList()
)

data class LineupItem(
    val team: TeamInfo,
    val formation: String?,
    val startXI: List<LineupPlayerWrapper>?,
    val substitutes: List<LineupPlayerWrapper>?
)

data class LineupPlayerWrapper(
    val player: LineupPlayer
)

data class LineupPlayer(
    val id: Int?,
    val name: String,
    val number: Int?,
    val pos: String?
)

data class TeamsSearchResponse(
    val response: List<TeamSearchItem> = emptyList()
)

data class TeamSearchItem(
    val team: TeamInfo,
    val venue: VenueInfo? = null
)

data class VenueInfo(
    val name: String? = null,
    val city: String? = null
)
