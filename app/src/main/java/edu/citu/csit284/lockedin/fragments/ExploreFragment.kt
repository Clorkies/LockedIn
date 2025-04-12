package edu.citu.csit284.lockedin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import edu.citu.csit284.lockedin.ExploreArticleActivity
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Article
import edu.citu.csit284.lockedin.helper.ArticleCustomListView

class ExploreFragment : Fragment() {

    private var caller: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {startActivity(Intent(requireContext(), ProfileActivity::class.java)) }

        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "live" -> findNavController().navigate(R.id.liveFragment)
                "explore" -> {}
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }



        val listView = view.findViewById<ListView>(R.id.articleListView)
        val articleList = listOf(
            Article(R.drawable.img_sample, "Article title 1", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample2, "Article title 2", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample3, "Article title 3", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample, "Article title 4", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample2, "Article title 5", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample3, "Article title 6", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample, "Article title 4", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample2, "Article title 5", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample3, "Article title 6", getString(R.string.lorem_ipsum))
        )

        listView.adapter = ArticleCustomListView(
            requireContext(),
            articleList,
            onClick = {
                val intent = Intent(requireContext(), ExploreArticleActivity::class.java).apply {
                    putExtra("imageResource", it.imgResId)
                    putExtra("title", it.title)
                    putExtra("articleText", it.articleText)
                    putExtra("caller", "explore")
                }
                startActivity(intent)
            }
        )
    }
}
