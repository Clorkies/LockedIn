package edu.citu.csit284.lockedin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import edu.citu.csit284.lockedin.ExploreArticleActivity
import edu.citu.csit284.lockedin.MatchDetailsActivity
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Article
import edu.citu.csit284.lockedin.data.NewsResponse
import edu.citu.csit284.lockedin.helper.ArticleCustomListView
import edu.citu.csit284.lockedin.util.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LandingFragment : Fragment() {

    private var caller: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener { startActivity(Intent(requireContext(), ProfileActivity::class.java)) }

        val listView = view.findViewById<ListView>(R.id.articleListView)

        fetchArticles(listView)

        val matchDetail = view.findViewById<FrameLayout>(R.id.ongoingMatch)
        matchDetail.setOnClickListener { startActivity(Intent(requireContext(), MatchDetailsActivity::class.java)) }
    }

    private fun fetchArticles(listView: ListView) {
        val retrofit = RetrofitClient.newsApiService
        retrofit.getEsportsArticles("esports", "6063a8b3c2624db09a5d6410d994e611").enqueue(object :
            Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.map {
                        val articleText = it.articleText
                        Article(it.imgResId, it.title, articleText)
                    } ?: emptyList()

                    listView.adapter = ArticleCustomListView(
                        requireContext(),
                        articles,
                        onClick = {
                            val intent = Intent(requireContext(), ExploreArticleActivity::class.java).apply {
                                putExtra("imageResource", it.imgResId)
                                putExtra("title", it.title)
                                putExtra("articleText", it.articleText)
                                putExtra("caller", "landing")
                            }
                            startActivity(intent)
                        }
                    )
                } else {
                    // Handle unsuccessful response
                    Toast.makeText(requireContext(), "Failed to load articles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}

