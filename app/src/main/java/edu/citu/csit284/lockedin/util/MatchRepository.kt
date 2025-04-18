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
    suspend fun getUpcomingLoLMatches(): List<Match> {
        return try {
            apiService.getUpcomingLoLMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUpcomingCSGOMatches(): List<Match> {
        return try {
            apiService.getUpcomingCSGOMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUpcomingDotaMatches(): List<Match> {
        return try {
            apiService.getUpcomingDotaMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUpcomingMLBBMatches(): List<Match> {
        return try {
            apiService.getUpcomingMLBBMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getUpcomingOverwatchMatches(): List<Match> {
        return try {
            apiService.getUpcomingOverwatchMatches(apiKey)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLiveMatches(vararg games: String): List<Match> {
        return try {
            val matches = mutableListOf<Match>()

            if (games.contains("valorant")) {
                matches.addAll(apiService.getValorantLiveMatches(apiKey))
            }

            if (games.contains("lol")) {
                matches.addAll(apiService.getLoLLiveMatches(apiKey))
            }

            if (games.contains("csgo")) {
                matches.addAll(apiService.getCSGOLiveMatches(apiKey))
            }

            if (games.contains("dota2")) {
                matches.addAll(apiService.getDotaLiveMatches(apiKey))
            }
            if (games.contains("mlbb")) {
                matches.addAll(apiService.getMLBBLiveMatches(apiKey))
            }
            if (games.contains("overwatch")) {
                matches.addAll(apiService.getOverwatchLiveMatches(apiKey))
            }

            if (games.isEmpty()) {
                matches.addAll(apiService.getLiveMatches(apiKey))
            }

            matches
        } catch (e: Exception) {
            emptyList()
        }
    }

}