package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.BuildConfig
import edu.citu.csit284.lockedin.data.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    fun getEsportsArticles(
        @Query("q") query: String = "(esports OR \"competitive gaming\" OR \"video games\" OR gaming) -football -soccer -basketball -baseball -cricket -rugby -NHL -NFL -NBA -MLB -golf -tennis -olympics",
        @Query("excludeDomains") excludeDomains: String = "espn.com,sports.yahoo.com,cbssports.com,nbcsports.com,bbc.com,businessinsider.com,npr.org",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en"
//        @Query("sortBy") sortBy: String = "publishedAt"
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun getGameSpecificArticles(
        @Query("q") gameQuery: String,
        @Query("excludeDomains") excludeDomains: String = "espn.com,sports.yahoo.com,cbssports.com,nbcsports.com,bbc.com,businessinsider.com,npr.org",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en",
//        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 50
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun getTournamentArticles(
        @Query("q") query: String = "(tournament OR championship OR league) AND (esports OR gaming)",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): Call<NewsResponse>

    @GET("v2/top-headlines")
    fun getGamingHeadlines(
        @Query("category") category: String = "technology",
        @Query("q") query: String = "gaming OR videogames",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en"
    ): Call<NewsResponse>
}