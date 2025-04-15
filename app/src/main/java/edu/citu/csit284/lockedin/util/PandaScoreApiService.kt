package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.data.Match
import retrofit2.http.GET
import retrofit2.http.Query

interface PandaScoreApiService {
    @GET("matches/upcoming")
    suspend fun getUpcomingMatches(
        @Query("token") apiToken: String,
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
    @GET("matches/upcoming")
    suspend fun getUpcomingValorantMatches(
        @Query("token") apiToken: String,
        @Query("filter[videogame]") videogame: String = "valorant",
        @Query("per_page") perPage: Int = 10,
    ): List<Match>
}
// Pair(1, "valorant")       // "valorant"
//Pair(2, "league")         // "lol"
//Pair(3, "csgo")           // "csgo"
//Pair(4, "dota")           // "dota2"
//Pair(5, "rivals")         // "marvel-rivals"
//Pair(6, "overwatch")      // "overwatch"