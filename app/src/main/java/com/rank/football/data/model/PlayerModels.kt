package com.rank.football.data.model

data class PlayerResponse(
    val response: List<PlayerProfileItem> = emptyList()
)

data class PlayerProfileItem(
    val player: ProfilePlayerDetail,
    val statistics: List<PlayerSeasonStats>? = null
)

data class ProfilePlayerDetail(
    val id: Int? = null,
    val name: String,
    val age: Int? = null,
    val nationality: String? = null,
    val height: String? = null,
    val photo: String? = null
)

data class PlayerSeasonStats(
    val team: TeamInfo,
    val league: LeagueInfo,
    val games: PlayerGames? = null,
    val goals: PlayerGoals? = null
)

data class PlayerGames(val appearences: Int? = null, val minutes: Int? = null)
data class PlayerGoals(val total: Int? = null, val assists: Int? = null)

data class TeamStatsResponse(
    val response: List<TeamStatsItem> = emptyList()
)

data class TeamStatsItem(
    val league: LeagueInfo,
    val team: TeamInfo,
    val form: String? = null
)

/** Lightweight player summary for profile screen display. */
data class PlayerSummary(
    val name: String,
    val statsSummary: String
)
