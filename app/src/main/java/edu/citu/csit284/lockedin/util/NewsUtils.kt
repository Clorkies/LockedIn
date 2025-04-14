package edu.citu.csit284.lockedin.util
import android.content.Context
import android.content.Intent
import android.widget.ListView
import android.widget.Toast
import edu.citu.csit284.lockedin.helper.ArticleAdapter
import edu.citu.csit284.lockedin.ExploreArticleActivity
import edu.citu.csit284.lockedin.data.NewsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                val articles = response.body()?.articles ?: emptyList()

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
