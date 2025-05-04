package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.BuildConfig
import edu.citu.csit284.lockedin.data.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    fun getEsportsArticles(
        @Query("q") query: String = "(esports OR \"competitive gaming\" OR \"video games\" OR gaming) -football -soccer -basketball -baseball -cricket -rugby -NHL -NFL -NBA -MLB -golf -tennis -olympics -sports",
        @Query("excludeDomains") excludeDomains: String = "espn.com,sports.yahoo.com,cbssports.com,nbcsports.com,bbc.com,businessinsider.com,npr.org",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 100
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun getGameSpecificArticles(
        @Query("q") gameQuery: String,
        @Query("excludeDomains") excludeDomains: String = "espn.com,sports.yahoo.com,cbssports.com,nbcsports.com,bbc.com,businessinsider.com,npr.org",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "relevancy",
        @Query("searchIn") searchIn: String = "title,description",
        @Query("pageSize") pageSize: Int = 100
    ): Call<NewsResponse>

    @GET("v2/everything")
    fun searchArticles(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "relevancy",
        @Query("pageSize") pageSize: Int = 100
    ): Call<NewsResponse>
}