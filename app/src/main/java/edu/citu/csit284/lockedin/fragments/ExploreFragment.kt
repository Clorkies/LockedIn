package edu.citu.csit284.lockedin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.LoadingAnimationUtil
import edu.citu.csit284.lockedin.util.fetchArticles
import edu.citu.csit284.lockedin.util.fetchBookmarkedArticles

class ExploreFragment : Fragment() {

    private var caller: String? = null
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var articlesContainer: FrameLayout

    private lateinit var bookmarkedListButton: ImageButton
    private lateinit var game1ListButton: Button
    private lateinit var game2ListButton: Button
    private lateinit var game3ListButton: Button

    private lateinit var listView: ListView

    private var currentCategory = "game1" // Default category
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

        view.findViewById<ImageButton>(R.id.button_profile).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "live" -> findNavController().navigate(R.id.liveFragment)
                "explore" -> {}
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        loadingView1 = view.findViewById(R.id.loadingView1)
        loadingView2 = view.findViewById(R.id.loadingView2)
        noInternetBox = view.findViewById(R.id.noInternetBox)
        noInternetBox.visibility = View.GONE
        articlesContainer = view.findViewById(R.id.articlesList)

        LoadingAnimationUtil.setupLoadingViews(requireContext(), loadingView1, loadingView2)
        LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, true)

        listView = view.findViewById(R.id.articleListView)

        loadArticles("game1")

        articlesContainer.translationY = 0f

        bookmarkedListButton = view.findViewById(R.id.bookmarkedList)
        game1ListButton = view.findViewById(R.id.game1List)
        game2ListButton = view.findViewById(R.id.game2List)
        game3ListButton = view.findViewById(R.id.game3List)

        updateButtonStyles("game1")

        bookmarkedListButton.setOnClickListener {
            if (currentCategory != "bookmarked") {
                switchCategory("bookmarked")
            }
        }

        game1ListButton.setOnClickListener {
            if (currentCategory != "game1") {
                switchCategory("game1")
            }
        }

        game2ListButton.setOnClickListener {
            if (currentCategory != "game2") {
                switchCategory("game2")
            }
        }

        game3ListButton.setOnClickListener {
            if (currentCategory != "game3") {
                switchCategory("game3")
            }
        }
    }
    private fun switchCategory(newCategory: String) {
        articlesContainer.animate()
            .translationY(1770f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, true)

                currentCategory = newCategory

                updateButtonStyles(newCategory)

                loadArticles(newCategory)

                articlesContainer.postDelayed({
                    articlesContainer.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }, 200)
            }
            .start()
    }

    private fun updateButtonStyles(activeCategory: String) {
        val inactiveBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        bookmarkedListButton.background = inactiveBackground
        bookmarkedListButton.setImageResource(R.drawable.icon_bookmark_checked)
        game1ListButton.background = inactiveBackground
        game2ListButton.background = inactiveBackground
        game3ListButton.background = inactiveBackground

        game1ListButton.setTextColor(inactiveTextColor)
        game2ListButton.setTextColor(inactiveTextColor)
        game3ListButton.setTextColor(inactiveTextColor)

        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg_selected)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.bg)

        when (activeCategory) {
            "bookmarked" -> {
                bookmarkedListButton.background = activeBackground
                bookmarkedListButton.setImageResource(R.drawable.icon_bookmark_dark)
            }
            "game1" -> {
                game1ListButton.background = activeBackground
                game1ListButton.setTextColor(activeTextColor)
            }
            "game2" -> {
                game2ListButton.background = activeBackground
                game2ListButton.setTextColor(activeTextColor)
            }
            "game3" -> {
                game3ListButton.background = activeBackground
                game3ListButton.setTextColor(activeTextColor)
            }
        }
    }

    private fun loadArticles(category: String) {
        when (category) {
            "bookmarked" -> {
                fetchBookmarkedArticles(requireContext(), listView, caller = "explore") { hasArticles ->
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = View.GONE
                }
            }
            "game1" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
                }
            }
            "game2" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
                }
            }
            "game3" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LoadingAnimationUtil.cancelAnimations()
    }
}
