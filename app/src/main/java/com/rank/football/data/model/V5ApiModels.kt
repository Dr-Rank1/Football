package com.rank.football.data.model

import com.google.gson.annotations.SerializedName

data class InjuriesResponse(
    val response: List<InjuryItem> = emptyList()
)

data class InjuryItem(
    val player: InjuryPlayer,
    val team: InjuryTeam,
    val fixture: InjuryFixture,
    val type: String? = null,
    val reason: String? = null
)

data class InjuryPlayer(
    val id: Int?,
    val name: String?,
    val photo: String? = null,
    val type: String? = null,
    val reason: String? = null
)

data class InjuryTeam(val id: Int?, val name: String?, val logo: String? = null)
data class InjuryFixture(val id: Int?, val date: String? = null)

data class TransfersResponse(
    val response: List<TransferItem> = emptyList()
)

data class TransferItem(
    val player: TransferPlayer,
    val update: String? = null,
    val transfers: List<TransferDetail> = emptyList()
)

data class TransferPlayer(
    val id: Int?,
    val name: String?,
    val photo: String? = null
)

data class TransferDetail(
    val date: String? = null,
    val type: String? = null,
    val teams: TransferTeams? = null
)

data class TransferTeams(
    @SerializedName("in") val teamIn: TransferTeamRef? = null,
    @SerializedName("out") val teamOut: TransferTeamRef? = null
)

data class TransferTeamRef(val id: Int?, val name: String?, val logo: String? = null)

data class TeamStatisticsResponse(
    val response: List<TeamStatisticsItem> = emptyList()
)

data class TeamStatisticsItem(
    val league: LeagueRef?,
    val team: TeamRef?,
    val form: String? = null,
    val fixtures: TeamFixturesStats? = null,
    val goals: TeamGoalsStats? = null,
    val biggest: TeamBiggestStats? = null,
    val clean_sheet: TeamCleanSheetStats? = null,
    val failed_to_score: TeamFailedScoreStats? = null,
    val penalty: TeamPenaltyStats? = null,
    val cards: TeamCardsStats? = null
)

data class LeagueRef(val id: Int?, val name: String?, val season: Int? = null)
data class TeamRef(val id: Int?, val name: String?, val logo: String? = null)
data class TeamFixturesStats(val played: HomeAwayInt? = null, val wins: HomeAwayInt? = null)
data class TeamGoalsStats(
    val `for`: HomeAwayGoalDetail? = null,
    val against: HomeAwayGoalDetail? = null
)
data class HomeAwayInt(val home: Int? = null, val away: Int? = null, val total: Int? = null)
data class HomeAwayGoalDetail(val total: HomeAwayInt? = null, val minute: Map<String, MinuteStat>? = null)
data class MinuteStat(val total: Int? = null, val percentage: String? = null)
data class TeamBiggestStats(val streak: StreakStats? = null)
data class StreakStats(val wins: Int? = null, val draws: Int? = null, val loses: Int? = null)
data class TeamCleanSheetStats(val home: Int? = null, val away: Int? = null, val total: Int? = null)
data class TeamFailedScoreStats(val home: Int? = null, val away: Int? = null, val total: Int? = null)
data class TeamPenaltyStats(val scored: PenaltyDetail? = null, val missed: PenaltyDetail? = null, val total: Int? = null)
data class PenaltyDetail(val total: Int? = null, val percentage: String? = null)
data class TeamCardsStats(val yellow: Map<String, MinuteStat>? = null, val red: Map<String, MinuteStat>? = null)
