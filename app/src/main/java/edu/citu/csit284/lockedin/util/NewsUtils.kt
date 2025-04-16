package edu.citu.csit284.lockedin.util
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.helper.ArticleAdapter
import edu.citu.csit284.lockedin.activities.ExploreArticleActivity
import edu.citu.csit284.lockedin.data.Article
import edu.citu.csit284.lockedin.data.NewsResponse
import edu.citu.csit284.lockedin.util.FilterUtil.getGameKeywords
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Date

private lateinit var bookmarks: List<Map<String, Any>>
private lateinit var con: Context
private lateinit var listV: ListView
private lateinit var onComp: (hasArticles: Boolean) -> Unit

fun fetchArticles(
    context: Context,
    listView: ListView,
    caller: String = "landing",
    onComplete: (hasInternet: Boolean) -> Unit = {}
) {
    val apiService = RetrofitClient.newsApiService
    apiService.getEsportsArticles().enqueue(object : Callback<NewsResponse> {
        override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
            if (response.isSuccessful) {
                val allArticles = response.body()?.articles ?: emptyList()

                val displayArticles = allArticles.filter { isSafeArticle(it) }.shuffled()

                Log.d("NSFW_Filter", "Filtered out ${allArticles.size - displayArticles.size} NSFW articles.")

                listView.adapter = ArticleAdapter(context, displayArticles)

                listView.setOnItemClickListener { _, _, position, _ ->
                    val article = displayArticles[position]
                    val intent = Intent(context, ExploreArticleActivity::class.java).apply {
                        putExtra("imageUrl", article.urlToImage)
                        putExtra("title", article.title)
                        putExtra("articleText", article.description)
                        putExtra("date", article.publishedAt)
                        putExtra("articleUrl", article.url)
                        putExtra("articleAuthor", article.author)
                        putExtra("caller", caller)
                    }
                    context.startActivity(intent)
                }
            } else {
                val message = getAPITimeRemaining(response)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            onComplete(true)
        }

        override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
            onComplete(false)
        }
    })
}


fun fetchArticlesSpecific(
    context: Context,
    listView: ListView,
    caller: String = "landing",
    gameName: String = "valorant",
    onComplete: (hasInternet: Boolean) -> Unit = {}
) {
    val apiService = RetrofitClient.newsApiService

    val keywords = getGameKeywords(gameName)
    val queryBuilder = StringBuilder("(")

    keywords.forEachIndexed { index, keyword ->
        val formattedKeyword = if (keyword.contains(" ")) "\"$keyword\"" else keyword
        queryBuilder.append(formattedKeyword)

        if (index < keywords.size - 1) {
            queryBuilder.append(" OR ")
        }
    }

    queryBuilder.append(") -football -soccer -basketball -baseball -cricket -tennis -NFL -NBA -MLB")

    val finalQuery = queryBuilder.toString()
    Log.d("ArticleFetcher", "Game query for $gameName: $finalQuery")

    apiService.getGameSpecificArticles(gameQuery = finalQuery).enqueue(object : Callback<NewsResponse> {
        override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
            if (response.isSuccessful) {
                val allArticles = response.body()?.articles ?: emptyList()

                val displayArticles = if (caller == "explore" && gameName.isNotEmpty()) {
                    val filtered = FilterUtil.filterArticlesByGame(allArticles, gameName)

                    if (filtered.isEmpty()) {
                        Toast.makeText(context, "No articles found for $gameName", Toast.LENGTH_SHORT).show()
                    }

                    filtered.shuffled()
                } else {
                    allArticles.shuffled()
                }

                listView.adapter = ArticleAdapter(context, displayArticles)

                listView.setOnItemClickListener { _, _, position, _ ->
                    val article = displayArticles[position]
                    val intent = Intent(context, ExploreArticleActivity::class.java).apply {
                        putExtra("imageUrl", article.urlToImage)
                        putExtra("title", article.title)
                        putExtra("articleText", article.description)
                        putExtra("date", article.publishedAt)
                        putExtra("articleUrl", article.url)
                        putExtra("articleAuthor", article.author)
                        putExtra("caller", caller)
                    }
                    context.startActivity(intent)
                }
            } else {
                val message = getAPITimeRemaining(response)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            onComplete(true)
        }

        override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
            Log.e("ArticleFetcher", "API call failed", t)
            onComplete(false)
        }
    })
}
fun fetchBookmarkedArticles(
    context: Context,
    listView: ListView,
    caller: String = "explore",
    onComplete: (hasArticles: Boolean) -> Unit = {}
) {
    con = context
    listV = listView
    onComp = onComplete

    val sharedPref = context.getSharedPreferences("User", Context.MODE_PRIVATE)
    val username = sharedPref.getString("username", null)

    val users = Firebase.firestore.collection("users")

    users.whereEqualTo("username", username)
        .limit(1)
        .get()
        .addOnSuccessListener { userDocs ->
            if (userDocs.documents.isEmpty()) {
                listView.adapter = ArticleAdapter(context, emptyList())
                onComplete(false)
                return@addOnSuccessListener
            }

            val userDoc = userDocs.documents[0]
            bookmarks = userDoc.get("bookmarks") as? List<Map<String, Any>> ?: listOf()

            if (bookmarks.isEmpty()) {
                listView.adapter = ArticleAdapter(context, emptyList())
                onComplete(false)
                return@addOnSuccessListener
            }

            var articles = getArticles()

            listView.adapter = ArticleAdapter(context, articles)

            listView.setOnItemClickListener { _, _, position, _ ->
                val article = articles[position]
                val intent = Intent(context, ExploreArticleActivity::class.java).apply {
                    putExtra("imageUrl", article.urlToImage)
                    putExtra("title", article.title)
                    putExtra("articleText", article.description)
                    putExtra("date", article.publishedAt)
                    putExtra("articleUrl", article.url)
                    putExtra("articleAuthor", article.author)
                    putExtra("caller", caller)
                }
                context.startActivity(intent)
            }

            onComplete(true)
        }
        .addOnFailureListener { _ ->
            Toast.makeText(context, "Failed to load bookmarks", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
}

fun getArticles(): List<Article> {
    val articles = bookmarks.map { bookmark ->
        Article(
            title = bookmark["title"] as? String ?: "Untitled",
            description = bookmark["description"] as? String ?: "Untitled",
            url = bookmark["url"] as? String ?: "",
            urlToImage = bookmark["imageUrl"] as? String ?: "",
            publishedAt = bookmark["date"] as? String ?: "",
            author = bookmark["author"] as? String?: ""
        )
    }
    return articles
}

fun getGameNameById(id: Int): String {
    return when (id) {
        1 -> "valorant"
        2 -> "league"
        3 -> "csgo"
        4 -> "dota"
        5 -> "rivals"
        6 -> "overwatch"
        else -> ""
    }
}

fun getAPITimeRemaining(response: Response<NewsResponse>) : String {
    val errorBody = response.errorBody()?.string() ?: ""
    Log.e("ArticleFetcher", "API error: ${response.code()} - $errorBody")

    val message = when {
        response.code() == 429 -> {
            val resetTime = response.headers().get("X-RateLimit-Reset")
            val retryAfter = response.headers().get("Retry-After")

            when {
                resetTime != null -> {
                    val resetTimeMillis = resetTime.toLongOrNull()?.times(1000) ?: 0L
                    if (resetTimeMillis > 0) {
                        val now = System.currentTimeMillis()
                        val resetDate = Date(resetTimeMillis)
                        val timeRemaining = (resetTimeMillis - now) / 60000

                        if (timeRemaining > 60) {
                            val hoursRemaining = timeRemaining / 60
                            "API limit reached. Please try again in $hoursRemaining hour(s)."
                        } else {
                            "API limit reached. Please try again in $timeRemaining minute(s)."
                        }
                    } else {
                        "API usage limit exceeded. Please try again at 12:00 PM."
                    }
                }
                retryAfter != null -> {
                    val secondsToWait = retryAfter.toIntOrNull() ?: 0
                    if (secondsToWait > 60) {
                        val minutesToWait = secondsToWait / 60
                        "API limit reached. Please try again in $minutesToWait minute(s)."
                    } else {
                        "API limit reached. Please try again in $secondsToWait second(s)."
                    }
                }
                else -> {
                    val calendar = Calendar.getInstance()
                    val now = calendar.timeInMillis

                    calendar.set(Calendar.HOUR_OF_DAY, 12)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)

                    if (now > calendar.timeInMillis) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }

                    val timeRemaining = (calendar.timeInMillis - now) / 60000
                    val hoursRemaining = timeRemaining / 60
                    val minutesRemaining = timeRemaining % 60

                    if (hoursRemaining > 0) {
                        "API limit reached. Please try again in $hoursRemaining hour(s) and $minutesRemaining minute(s)."
                    } else {
                        "API limit reached. Please try again in $minutesRemaining minute(s)."
                    }
                }
            }
        }
        errorBody.contains("rate limit", ignoreCase = true) ||
                errorBody.contains("quota exceeded", ignoreCase = true) ||
                errorBody.contains("usage limit", ignoreCase = true) ||
                errorBody.contains("too many requests", ignoreCase = true) -> {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            if (now > calendar.timeInMillis) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val timeRemaining = (calendar.timeInMillis - now) / 60000
            val hoursRemaining = timeRemaining / 60
            val minutesRemaining = timeRemaining % 60

            if (hoursRemaining > 0) {
                "API limit reached. Please try again in $hoursRemaining hour(s) and $minutesRemaining minute(s)."
            } else {
                "API limit reached. Please try again in $minutesRemaining minute(s)."
            }
        }
        response.code() == 401 || errorBody.contains("api key", ignoreCase = true) ->
            "API authentication error. Please check your API key."
        else -> "Failed to load articles. Error code: ${response.code()}"
    }
    return message
}

fun isSafeArticle(article: Article): Boolean {
    val combinedText = "${article.title} ${article.description}".lowercase()
    return nsfwKeywords.none { keyword -> combinedText.contains(keyword) }
}

val nsfwKeywords = listOf(
    "nsfw", "explicit", "nude", "nudity", "sexual", "sex", "porn", "xxx", "18+", "onlyfans",
    "lewd", "uncensored", "adult", "erotic", "naked", "boobs", "tits", "fetish", "kink", "r18"
)