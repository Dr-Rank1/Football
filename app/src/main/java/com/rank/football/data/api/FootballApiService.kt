package com.rank.football.data.api

import com.rank.football.data.model.EventsResponse
import com.rank.football.data.model.FixturesResponse
import com.rank.football.data.model.LeaguesResponse
import com.rank.football.data.model.LineupsResponse
import com.rank.football.data.model.StandingsResponse
import com.rank.football.data.model.StatisticsResponse
import com.rank.football.data.model.TeamsSearchResponse
import com.rank.football.data.model.PlayerResponse
import com.rank.football.data.model.TeamStatsResponse
import com.rank.football.data.model.InjuriesResponse
import com.rank.football.data.model.TransfersResponse
import com.rank.football.data.model.TeamStatisticsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    @GET("fixtures")
    suspend fun getFixture(
        @Query("id") fixtureId: Int
    ): FixturesResponse

    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("live") live: String = "all"
    ): FixturesResponse

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String,
        @Query("timezone") tz: String = "Africa/Nairobi"
    ): FixturesResponse

    @GET("fixtures")
    suspend fun getFixturesByLeague(
        @Query("league") leagueId: Int,
        @Query("season") season: Int,
        @Query("next") next: Int = 10
    ): FixturesResponse

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): StandingsResponse

    @GET("leagues")
    suspend fun getLeagues(
        @Query("current") current: Boolean = true
    ): LeaguesResponse

    @GET("leagues")
    suspend fun searchLeagues(
        @Query("search") query: String
    ): LeaguesResponse

    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int
    ): EventsResponse

    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int
    ): StatisticsResponse

    @GET("fixtures/lineups")
    suspend fun getFixtureLineups(
        @Query("fixture") fixtureId: Int
    ): LineupsResponse

    @GET("teams")
    suspend fun searchTeams(
        @Query("search") query: String
    ): TeamsSearchResponse

    @GET("teams/statistics")
    suspend fun getTeamStats(
        @Query("team") teamId: Int,
        @Query("season") season: Int,
        @Query("league") leagueId: Int
    ): TeamStatsResponse

    @GET("fixtures/headtohead")
    suspend fun getHeadToHead(
        @Query("h2h") h2h: String
    ): FixturesResponse

    @GET("players")
    suspend fun getPlayer(
        @Query("id") playerId: Int,
        @Query("season") season: Int
    ): PlayerResponse

    @GET("players/topscorers")
    suspend fun getTopScorers(
        @Query("league") leagueId: Int,
        @Query("season") season: Int
    ): PlayerResponse

    @GET("injuries")
    suspend fun getInjuries(
        @Query("fixture") fixtureId: Int
    ): InjuriesResponse

    @GET("transfers")
    suspend fun getTransfers(
        @Query("team") teamId: Int,
        @Query("season") season: Int
    ): TransfersResponse

    @GET("teams/statistics")
    suspend fun getTeamStatistics(
        @Query("team") teamId: Int,
        @Query("season") season: Int,
        @Query("league") leagueId: Int
    ): TeamStatisticsResponse
}
