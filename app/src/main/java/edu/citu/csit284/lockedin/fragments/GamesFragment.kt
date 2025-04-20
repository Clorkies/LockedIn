package edu.citu.csit284.lockedin.fragments

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.activities.MainActivity
import edu.citu.csit284.lockedin.data.Match
import edu.citu.csit284.lockedin.helper.BottomSpace
import edu.citu.csit284.lockedin.helper.UpcomingMatchAdapter
import edu.citu.csit284.lockedin.util.LoadingAnimationUtils
import edu.citu.csit284.lockedin.util.MatchRepository
import edu.citu.csit284.lockedin.util.setupHeaderScrollBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamesFragment : Fragment() {

    private var caller: String? = null
    private val users = Firebase.firestore.collection("users")
    private val matchRepository = MatchRepository()
    private lateinit var headerContainer: LinearLayout
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var loadingView3: View
    private lateinit var loadingView4: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UpcomingMatchAdapter
    private val matches = mutableListOf<Match>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private val games = listOf(
        1 to "valorant",
        2 to "lol",
        3 to "csgo",
        4 to "dota2",
        5 to "mlbb",
        6 to "overwatch"
    )
    private val gameMap: Map<Int, String> = games.toMap()
    private lateinit var btnGame1: LinearLayout
    private lateinit var btnGame1Text: TextView
    private lateinit var btnGame2: LinearLayout
    private lateinit var btnGame2Text: TextView
    private lateinit var btnGame3: LinearLayout
    private lateinit var btnGame3Text: TextView
    private var prefNames: List<String> = emptyList()

    private lateinit var sharedPref: SharedPreferences
    private var userInfo: String? = null
    private var currentCategory = "game1"
    private var previousCategory = "game1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
        sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        userInfo = sharedPref.getString("username", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvView)
        adapter = UpcomingMatchAdapter(matches)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.visibility = View.GONE

        // Para di matago behind the navbar ang last item sa scroll/listview
        val bottomSpace = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            110f,
            resources.displayMetrics
        ).toInt()
        recyclerView.addItemDecoration(BottomSpace(bottomSpace))
        ////

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            profileActivityLauncher.launch(Intent(requireContext(), ProfileActivity::class.java))
        }
        val sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        val userInfo = sharedPref.getString("username", "")
        var pfp: Int
        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
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
                    val rawList = document.get("favGames") as? List<Long>
                    prefNames = rawList
                        ?.map { it.toInt() }
                        ?.mapNotNull { gameMap[it] }
                        ?: emptyList()

                    btnGame1 = view.findViewById(R.id.game1Btn)
                    btnGame1Text = view.findViewById(R.id.game1)
                    btnGame2 = view.findViewById(R.id.game2Btn)
                    btnGame2Text = view.findViewById(R.id.game2)
                    btnGame3 = view.findViewById(R.id.game3Btn)
                    btnGame3Text = view.findViewById(R.id.game3)

                    setupFavoriteGames()

                    btnGame1.setOnClickListener { if (currentCategory != "game1") { switchCategory("game1", prefNames.getOrNull(0) ?: "valorant") } }
                    btnGame2.setOnClickListener { if (currentCategory != "game2") { switchCategory("game2", prefNames.getOrNull(1) ?: "lol") } }
                    btnGame3.setOnClickListener { if (currentCategory != "game3") { switchCategory("game3", prefNames.getOrNull(2) ?: "csgo") } }
                }
            }


        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> {}
                "forum" -> findNavController().navigate(R.id.forumFragment)
                "explore" -> findNavController().navigate(R.id.exploreFragment)
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        headerContainer = view.findViewById(R.id.headerContainer)
        loadingView1 = view.findViewById(R.id.loadingView1)
        loadingView2 = view.findViewById(R.id.loadingView2)
        loadingView3 = view.findViewById(R.id.loadingView3)
        loadingView4 = view.findViewById(R.id.loadingView4)
        noInternetBox = view.findViewById(R.id.noInternetBox)
        noInternetBox.visibility = View.GONE

        setupHeaderScrollBehavior(headerContainer, recyclerView)

        LoadingAnimationUtils.showLoading(requireContext(), loadingView1, loadingView2, true)
        LoadingAnimationUtils.showLoading(requireContext(), loadingView3, loadingView4, true)
    }

    private val profileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val currentId = (requireActivity() as MainActivity).navController.currentDestination?.id ?: return@registerForActivityResult

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()

        (requireActivity() as MainActivity).apply {
            isNavigatingFromCode = true
            try {
                navController.navigate(currentId, null, navOptions)
            } catch (_: IllegalArgumentException) {}
        }
    }

    private fun setupFavoriteGames() {
        if (prefNames.size >= 1) {
            setupGameButton(btnGame1, btnGame1Text, prefNames[0], 1)
        }
        if (prefNames.size >= 2) {
            setupGameButton(btnGame2, btnGame2Text, prefNames[1], 2)
        }
        if (prefNames.size >= 3) {
            setupGameButton(btnGame3, btnGame3Text, prefNames[2], 3)
        }

        updateButtonStyles(currentCategory)
        loadMatches(prefNames.getOrNull(0) ?: "valorant")
    }

    private fun setupGameButton(button: LinearLayout, textView: TextView, gameName: String, buttonIndex: Int) {
        button.setPadding(20)

        textView.tag = gameName
        textView.text = gameName.replaceFirstChar { it.uppercase() }
        val logoName = when (gameName.lowercase()) {
            "valorant" -> "valorant"
            "lol" -> "league"
            "csgo" -> "csgo"
            "dota2" -> "dota"
            "mlbb" -> "mlbb"
            "overwatch" -> "overwatch"
            else -> ""
        }
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
            "logo_$logoName",
            "drawable",
            requireContext().packageName
        )

        if (logoResId != 0) {
            logoView.setImageResource(logoResId)
        }

        button.tag = "game$buttonIndex"
    }

    private fun switchCategory(newCategory: String, newGame: String) {
        recyclerView.animate()
            .translationY(1770f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                LoadingAnimationUtils.showLoading(requireContext(), loadingView1, loadingView2, true)
                LoadingAnimationUtils.showLoading(requireContext(), loadingView3, loadingView4, true)
                currentCategory = newCategory
                updateButtonStyles(newCategory)
                loadMatches(newGame)
                recyclerView.postDelayed({
                    recyclerView.animate()
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
            "game1" -> setButtonActive(btnGame1, btnGame1.findViewWithTag("gameLogoImage1"), btnGame1Text, btnGame1Text)
            "game2" -> setButtonActive(btnGame2, btnGame2.findViewWithTag("gameLogoImage2"), btnGame2Text, btnGame2Text)
            "game3" -> setButtonActive(btnGame3, btnGame3.findViewWithTag("gameLogoImage3"), btnGame3Text, btnGame3Text)
        }
        previousCategory = activeCategory
    }

    private fun resetButtonToInactive(category: String) {
        val inactiveBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_bg)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        when (category) {
            "game1" -> {
                btnGame1.background = inactiveBackground
                btnGame1Text.setTextColor(inactiveTextColor)
                btnGame1Text.visibility = View.GONE
                btnGame1.findViewWithTag<ImageView>("gameLogoImage1")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame1, 250)
            }
            "game2" -> {
                btnGame2.background = inactiveBackground
                btnGame2Text.setTextColor(inactiveTextColor)
                btnGame2Text.visibility = View.GONE
                btnGame2.findViewWithTag<ImageView>("gameLogoImage2")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame2, 250)
            }
            "game3" -> {
                btnGame3.background = inactiveBackground
                btnGame3Text.setTextColor(inactiveTextColor)
                btnGame3Text.visibility = View.GONE
                btnGame3.findViewWithTag<ImageView>("gameLogoImage3")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame3, 250)
            }
        }
    }

    private fun setButtonActive(button: View, iconView: View?, textView: View, textViewForColor: TextView? = null) {
        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg_selected)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.bg)

        animateButtonWidth(button, 350)

        button.postDelayed({
            button.background = activeBackground
            iconView?.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textViewForColor?.setTextColor(activeTextColor)
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

    private fun loadMatches(game: String) {
        coroutineScope.launch {
            val upcomingMatches = withContext(Dispatchers.IO) {
                when (game.lowercase()) {
                    "valorant" -> matchRepository.getUpcomingValorantMatches()
                    "lol" -> matchRepository.getUpcomingLoLMatches()
                    "csgo" -> matchRepository.getUpcomingCSGOMatches()
                    "dota2" -> matchRepository.getUpcomingDotaMatches()
                    "mlbb" -> matchRepository.getUpcomingMLBBMatches()
                    "overwatch" -> matchRepository.getUpcomingOverwatchMatches()
                    else -> emptyList()
                }
            }
            LoadingAnimationUtils.showLoading(requireContext(), loadingView1, loadingView2, false)
            LoadingAnimationUtils.showLoading(requireContext(), loadingView3, loadingView4, false)
            recyclerView.visibility = View.VISIBLE
            recyclerView.scrollToPosition(0)
            matches.clear()
            matches.addAll(upcomingMatches)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        LoadingAnimationUtils.cancelAnimations()
    }
}