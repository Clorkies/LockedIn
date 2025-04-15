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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                var articles = response.body()?.articles ?: emptyList()
                articles = articles.shuffled()

                listView.adapter = ArticleAdapter(context, articles)

                listView.setOnItemClickListener { _, _, position, _ ->
                    val article = articles[position]
                    val intent = Intent(context, ExploreArticleActivity::class.java).apply {
                        putExtra("imageUrl", article.urlToImage)
                        putExtra("title", article.title)
                        putExtra("articleText", article.description)
                        putExtra("date", article.publishedAt)
                        putExtra("articleUrl", article.url)
                        putExtra("caller", caller)
                    }
                    context.startActivity(intent)
                }
            } else {
                Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
            }
            onComplete(true)
        }

        override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
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
            publishedAt = bookmark["date"] as? String ?: ""
        )
    }
    return articles
}