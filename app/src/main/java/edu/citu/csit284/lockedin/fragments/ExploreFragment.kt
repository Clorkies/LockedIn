package edu.citu.csit284.lockedin.fragments

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.FilterUtil
import edu.citu.csit284.lockedin.util.LoadingAnimationUtil
import edu.citu.csit284.lockedin.util.fetchArticles
import edu.citu.csit284.lockedin.util.fetchArticlesSpecific
import edu.citu.csit284.lockedin.util.fetchBookmarkedArticles
import edu.citu.csit284.lockedin.util.getGameNameById

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

    private var currentCategory = "game1"
    private var previousCategory = "game1"

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
                        1 -> { btnProfile.setImageResource(R.drawable.red_pfp) }
                        2 -> { btnProfile.setImageResource(R.drawable.default_pfp) }
                        3 -> { btnProfile.setImageResource(R.drawable.green_pfp) }
                        4 -> { btnProfile.setImageResource(R.drawable.blue_pfp) }
                    }
                }
            }

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "forum" -> findNavController().navigate(R.id.forumFragment)
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

        setupFavoriteGames()

        bookmarkedListButton.setOnClickListener { if (currentCategory != "bookmarked") { switchCategory("bookmarked") } }
        game1ListButton.setOnClickListener { if (currentCategory != "game1") { switchCategory("game1") } }
        game2ListButton.setOnClickListener { if (currentCategory != "game2") { switchCategory("game2") } }
        game3ListButton.setOnClickListener { if (currentCategory != "game3") { switchCategory("game3") } }
    }

    private fun setupFavoriteGames() {
        val sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        val username = sharedPref.getString("username", "")

        if (username.isNullOrEmpty()) return

        users.whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                val document = documents.documents[0]

                val rawFaveGames = document.get("favGames")
                Log.d("ExploreFragment", "Raw faveGames: $rawFaveGames")

                val faveGames = when (rawFaveGames) {
                    is List<*> -> {
                        val result = rawFaveGames.mapNotNull {
                            when (it) {
                                is Long -> it.toInt()
                                is Int -> it
                                is Double -> it.toInt()
                                is String -> it.toIntOrNull()
                                else -> null
                            }
                        }
                        Log.d("ExploreFragment", "Converted faveGames: $result")
                        if (result.size >= 3) result else listOf(1, 3, 6)
                    }
                    else -> {
                        Log.d("ExploreFragment", "Using default faveGames")
                        listOf(1, 3, 6)
                    }
                }

                Log.d("ExploreFragment", "Final faveGames: $faveGames")

                if (faveGames.size >= 1) {
                    setupGameButton(game1ListButton, game1ListButtonText, faveGames[0], 1)
                    Log.d("ExploreFragment", "Set game1 button with ID: ${faveGames[0]}")
                }
                if (faveGames.size >= 2) {
                    setupGameButton(game2ListButton, game2ListButtonText, faveGames[1], 2)
                    Log.d("ExploreFragment", "Set game2 button with ID: ${faveGames[1]}")
                }
                if (faveGames.size >= 3) {
                    setupGameButton(game3ListButton, game3ListButtonText, faveGames[2], 3)
                    Log.d("ExploreFragment", "Set game3 button with ID: ${faveGames[2]}")
                }

                Log.d("ExploreFragment", "game1 tag: ${game1ListButtonText.tag}")
                Log.d("ExploreFragment", "game2 tag: ${game2ListButtonText.tag}")
                Log.d("ExploreFragment", "game3 tag: ${game3ListButtonText.tag}")

                updateButtonStyles(currentCategory)
                loadArticles(currentCategory)
            }
            .addOnFailureListener { exception ->
                Log.e("ExploreFragment", "Error getting favorite games", exception)
            }
    }

    private fun setupGameButton(button: LinearLayout, textView: TextView, gameId: Int, buttonIndex: Int) {
        val gameName = getGameNameById(gameId)
        Log.d("ExploreFragment", "Setting up button $buttonIndex with gameId: $gameId, name: $gameName")

        button.setPadding(20)

        textView.tag = gameId
        Log.d("ExploreFragment", "Button $buttonIndex textView tag set to: ${textView.tag}")

        var logoView = button.findViewWithTag<ImageView>("gameLogoImage$buttonIndex")
        if (logoView == null) {
            logoView = ImageView(requireContext())
            logoView.tag = "gameLogoImage$buttonIndex"
            logoView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            button.addView(logoView, 0)
        }

        val logoResId = resources.getIdentifier(
            "logo_$gameName",
            "drawable",
            requireContext().packageName
        )

        if (logoResId != 0) {
            logoView.setImageResource(logoResId)
        }

        button.tag = gameName

        button.setOnClickListener { if (currentCategory != "game$buttonIndex") { switchCategory("game$buttonIndex") } }
    }

    private fun getGameIdForButton(buttonIndex: Int): Int {
        val tagValue = when (buttonIndex) {
            1 -> game1ListButtonText.tag
            2 -> game2ListButtonText.tag
            3 -> game3ListButtonText.tag
            else -> null
        }

        Log.d("ExploreFragment", "getGameIdForButton($buttonIndex) - raw tag: $tagValue")

        val gameId = when (tagValue) {
            is Int -> tagValue
            is Long -> tagValue.toInt()
            is String -> tagValue.toIntOrNull() ?: when(buttonIndex) {1 -> 1; 2 -> 3; 3 -> 6; else -> 1 }
            else -> when(buttonIndex) { 1 -> 1; 2 -> 3; 3 -> 6; else -> 1 }
        }

        Log.d("ExploreFragment", "getGameIdForButton($buttonIndex) returning: $gameId")
        return gameId
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
        resetButtonToInactive(previousCategory)
        when (activeCategory) {
            "bookmarked" -> setButtonActive(bookmarkedListButton, bookmarkedListImage, bookmarkedListText, null, null)
            "game1" -> setButtonActive(game1ListButton, game1ListButton.findViewWithTag("gameLogoImage1"), game1ListButtonText, game1ListButtonText, game1ListButton.tag as? String)
            "game2" -> setButtonActive(game2ListButton, game2ListButton.findViewWithTag("gameLogoImage2"), game2ListButtonText, game2ListButtonText, game2ListButton.tag as? String)
            "game3" -> setButtonActive(game3ListButton, game3ListButton.findViewWithTag("gameLogoImage3"), game3ListButtonText, game3ListButtonText, game3ListButton.tag as? String)
        }
        previousCategory = activeCategory
    }

    private fun resetButtonToInactive(category: String) {
        val inactiveBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_bg)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        when (category) {
            "bookmarked" -> {
                bookmarkedListButton.background = inactiveBackground
                bookmarkedListImage.setImageResource(R.drawable.icon_bookmark_checked)
                bookmarkedListImage.visibility = View.VISIBLE
                bookmarkedListText.visibility = View.GONE
                animateButtonWidth(bookmarkedListButton, 150)
            }
            "game1" -> {
                game1ListButton.background = inactiveBackground
                game1ListButtonText.setTextColor(inactiveTextColor)
                game1ListButtonText.visibility = View.GONE
                game1ListButton.findViewWithTag<ImageView>("gameLogoImage1")?.visibility = View.VISIBLE
                animateButtonWidth(game1ListButton, 150)
            }
            "game2" -> {
                game2ListButton.background = inactiveBackground
                game2ListButtonText.setTextColor(inactiveTextColor)
                game2ListButtonText.visibility = View.GONE
                game2ListButton.findViewWithTag<ImageView>("gameLogoImage2")?.visibility = View.VISIBLE
                animateButtonWidth(game2ListButton, 150)
            }
            "game3" -> {
                game3ListButton.background = inactiveBackground
                game3ListButtonText.setTextColor(inactiveTextColor)
                game3ListButtonText.visibility = View.GONE
                game3ListButton.findViewWithTag<ImageView>("gameLogoImage3")?.visibility = View.VISIBLE
                animateButtonWidth(game3ListButton, 150)
            }
        }
    }

    private fun setButtonActive(button: View, iconView: View?, textView: View, textViewForColor: TextView? = null, gameName: String? = null) {
        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg_selected)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.bg)

        animateButtonWidth(button, 350)

        button.postDelayed({
            button.background = activeBackground
            iconView?.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textViewForColor?.setTextColor(activeTextColor)

            if (!gameName.isNullOrEmpty() && textViewForColor != null) {
                textViewForColor.text = gameName.replaceFirstChar { it.uppercase() }
            }
        }, 300)
    }

    private fun animateButtonWidth(button: View, targetWidth: Int) {
        val animator = ValueAnimator.ofInt(button.width, targetWidth)
        animator.addUpdateListener { valueAnimator ->
            val params = button.layoutParams
            params.width = valueAnimator.animatedValue as Int
            button.layoutParams = params
        }
        animator.duration = 350
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
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
            "game1", "game2", "game3" -> {
                val gameIndex = category.substring(4).toInt()
                val gameId = getGameIdForButton(gameIndex)
                val gameName = FilterUtil.getGameNameById(gameId)

                fetchArticlesSpecific(requireContext(), listView, caller = "explore", gameName = gameName) { hasInternet ->
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