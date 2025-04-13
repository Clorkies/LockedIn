package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.BuildConfig
import edu.citu.csit284.lockedin.data.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/everything")
    fun getEsportsArticles(
        @Query("q") query: String = "esports",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): Call<NewsResponse>
}
