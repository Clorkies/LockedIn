package edu.citu.csit284.lockedin.util

import android.util.Log
import edu.citu.csit284.lockedin.BuildConfig
import edu.citu.csit284.lockedin.data.Match
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MatchRepository {
    private val apiKey = BuildConfig.PANDASCORE_API_KEY
    private val baseUrl = "https://api.pandascore.co/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build())
        .build()

    private val apiService = retrofit.create(PandaScoreApiService::class.java)

    suspend fun getUpcomingValorantMatches(): List<Match> {
        return try {
            apiService.getUpcomingValorantMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUpcomingMatches(): List<Match> {
        return try {
            apiService.getUpcomingMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }

}