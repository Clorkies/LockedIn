package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.data.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/everything")
    fun getEsportsArticles(
        @Query("q") query: String = "esports",
        @Query("apiKey") apiKey: String = "6063a8b3c2624db09a5d6410d994e611"
    ): Call<NewsResponse>
}
