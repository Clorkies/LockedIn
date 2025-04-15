package edu.citu.csit284.lockedin.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.LoadingAnimationUtil
import edu.citu.csit284.lockedin.util.fetchArticles
import edu.citu.csit284.lockedin.util.fetchBookmarkedArticles

class ExploreFragment : Fragment() {

    private var caller: String? = null
    private val users = Firebase.firestore.collection("users")
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var articlesContainer: FrameLayout
    private lateinit var noBookmarkBox: LinearLayout

    private lateinit var bookmarkedListButton: LinearLayout
    private lateinit var bookmarkedListImage: ImageView
    private lateinit var bookmarkedListText: TextView
    private lateinit var game1ListButton: LinearLayout
    private lateinit var game1ListButtonText: TextView
    private lateinit var game2ListButton: LinearLayout
    private lateinit var game2ListButtonText: TextView
    private lateinit var game3ListButton: LinearLayout
    private lateinit var game3ListButtonText: TextView

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

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
            requireActivity().supportFragmentManager.popBackStack()
        }
        val sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        val userInfo = sharedPref.getString("username","")
        var pfp : Int
        users
            .whereEqualTo("username",userInfo)
            .get()
            .addOnSuccessListener {documents ->
                if(!documents.isEmpty){
                    val document = documents.documents[0]
                    pfp = document.getLong("pfpID")?.toInt() ?: 2
                    when (pfp) {
                        1 -> {
                            btnProfile.setImageResource(R.drawable.red_pfp)
                        }
                        2 -> {
                            btnProfile.setImageResource(R.drawable.default_pfp)
                        }
                        3 -> {
                            btnProfile.setImageResource(R.drawable.green_pfp)
                        }
                        4 -> {
                            btnProfile.setImageResource(R.drawable.blue_pfp)
                        }
                    }
                }
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
        noBookmarkBox = view.findViewById(R.id.noBookmarksBox)
        noBookmarkBox.visibility = View.GONE

        LoadingAnimationUtil.setupLoadingViews(requireContext(), loadingView1, loadingView2)
        LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, true)

        listView = view.findViewById(R.id.articleListView)

        loadArticles("game1")

        articlesContainer.translationY = 0f

        bookmarkedListButton = view.findViewById(R.id.bookmarkedList)
        bookmarkedListImage = view.findViewById(R.id.bookmarkedListImage)
        bookmarkedListText = view.findViewById(R.id.bookmarkListText)
        game1ListButton = view.findViewById(R.id.game1List)
        game1ListButtonText = view.findViewById(R.id.game1ListText)
        game2ListButton = view.findViewById(R.id.game2List)
        game2ListButtonText = view.findViewById(R.id.game2ListText)
        game3ListButton = view.findViewById(R.id.game3List)
        game3ListButtonText = view.findViewById(R.id.game3ListText)


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
        val inactiveBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_bg)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        bookmarkedListButton.background = inactiveBackground
        bookmarkedListImage.setImageResource(R.drawable.icon_bookmark_checked)
        bookmarkedListImage.visibility = View.VISIBLE
        bookmarkedListText.visibility = View.GONE
        game1ListButton.background = inactiveBackground
        game2ListButton.background = inactiveBackground
        game3ListButton.background = inactiveBackground

        game1ListButtonText.setTextColor(inactiveTextColor)
        game1ListButtonText.setText("V")
        game2ListButtonText.setTextColor(inactiveTextColor)
        game2ListButtonText.setText("C")
        game3ListButtonText.setTextColor(inactiveTextColor)
        game3ListButtonText.setText("R")

        var params = bookmarkedListButton.layoutParams
        params.width = 150
        bookmarkedListButton.layoutParams = params
        params = game1ListButton.layoutParams
        params.width = 150
        game1ListButton.layoutParams = params
        params = game2ListButton.layoutParams
        params.width = 150
        game2ListButton.layoutParams = params
        params = game3ListButton.layoutParams
        params.width = 150
        game3ListButton.layoutParams = params

        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg_selected)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.bg)

        when (activeCategory) {
            "bookmarked" -> {
                bookmarkedListButton.background = activeBackground
                params = bookmarkedListButton.layoutParams
                params.width = 350
                bookmarkedListButton.layoutParams = params
                bookmarkedListImage.setImageResource(R.drawable.icon_bookmark_dark)
                bookmarkedListImage.visibility = View.GONE
                bookmarkedListText.visibility = View.VISIBLE
            }
            "game1" -> {
                game1ListButton.background = activeBackground
                params = game1ListButton.layoutParams
                params.width = 350
                game1ListButton.layoutParams = params
                game1ListButtonText.setTextColor(activeTextColor)
                game1ListButtonText.setText("Valorant")
            }
            "game2" -> {
                game2ListButton.background = activeBackground
                params = game2ListButton.layoutParams
                params.width = 350
                game2ListButton.layoutParams = params
                game2ListButtonText.setTextColor(activeTextColor)
                game2ListButtonText.setText("CS:GO")
            }
            "game3" -> {
                game3ListButton.background = activeBackground
                params = game3ListButton.layoutParams
                params.width = 350
                game3ListButton.layoutParams = params
                game3ListButtonText.setTextColor(activeTextColor)
                game3ListButtonText.setText("Roblox")
            }
        }
    }

    private fun loadArticles(category: String) {
        when (category) {
            "bookmarked" -> {
                fetchBookmarkedArticles(requireContext(), listView, caller = "explore") { hasArticles ->
                    if (listView.adapter.count == 0) {
                        noBookmarkBox.visibility = View.VISIBLE
                    }
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = View.GONE
                }
            }
            "game1" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    noBookmarkBox.visibility = View.GONE
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
                }
            }
            "game2" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    noBookmarkBox.visibility = View.GONE
                    LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
                    noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
                }
            }
            "game3" -> {
                fetchArticles(requireContext(), listView, caller = "explore") { hasInternet ->
                    noBookmarkBox.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        if (currentCategory == "bookmarked") {
            loadArticles("bookmarked")
        }
    }
}
