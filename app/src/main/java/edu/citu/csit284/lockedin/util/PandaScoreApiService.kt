package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.data.Match
import retrofit2.http.GET
import retrofit2.http.Query

interface PandaScoreApiService {
    @GET("matches/running")
    suspend fun getLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("valorant/matches/running")
    suspend fun getValorantLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("lol/matches/running")
    suspend fun getLoLLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("csgo/matches/running")
    suspend fun getCSGOLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("dota2/matches/running")
    suspend fun getDotaLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("mlbb/matches/running")
    suspend fun getMLBBLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("ow/matches/running")
    suspend fun getOverwatchLiveMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("valorant/matches/upcoming")
    suspend fun getUpcomingValorantMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>

    @GET("lol/matches/upcoming")
    suspend fun getUpcomingLoLMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>

    @GET("csgo/matches/upcoming")
    suspend fun getUpcomingCSGOMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>

    @GET("dota2/matches/upcoming")
    suspend fun getUpcomingDotaMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>

    @GET("mlbb/matches/upcoming")
    suspend fun getUpcomingMLBBMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>

    @GET("ow/matches/upcoming")
    suspend fun getUpcomingOverwatchMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
}
// Pair(1, "valorant")       // "valorant"
//Pair(2, "league")         // "lol"
//Pair(3, "csgo")           // "csgo"
//Pair(4, "dota")           // "dota2"
//Pair(5, "rivals")         // "marvel-rivals"
//Pair(6, "overwatch")      // "overwatch"