package edu.citu.csit284.lockedin.fragments

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.activities.CreatePostActivity

class ForumFragment : Fragment() {

    private var caller: String? = null
    private val users = Firebase.firestore.collection("users")
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
    private val gamesMap = mapOf(
        1 to "valorant",
        2 to "lol",
        3 to "csgo",
        4 to "dota2",
        5 to "marvel-rivals",
        6 to "overwatch"
    )
    private lateinit var fabCreate : MaterialButton

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
        return inflater.inflate(R.layout.fragment_forum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "forum" -> {}
                "explore" -> findNavController().navigate(R.id.exploreFragment)
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnGame1 = view.findViewById(R.id.game1Btn)
        btnGame1Text = view.findViewById(R.id.game1)
        btnGame2 = view.findViewById(R.id.game2Btn)
        btnGame2Text = view.findViewById(R.id.game2)
        btnGame3 = view.findViewById(R.id.game3Btn)
        btnGame3Text = view.findViewById(R.id.game3)
        fabCreate = view.findViewById(R.id.fabCreate)
        fabCreate.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            val currentGame = getCurrentGame()
            intent.putExtra("currentGame", currentGame)
            startActivity(intent)
        }

        loadFavoriteGames()
    }

    private fun loadFavoriteGames() {
        userInfo?.let {
            users.whereEqualTo("username", it)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val rawList = document.get("favGames") as? List<Long>
                        prefNames = rawList
                            ?.map { it.toInt() }
                            ?.mapNotNull { gamesMap[it] }
                            ?: emptyList()

                        setupFavoriteGamesButtons()
                    }
                }
        }
    }

    private fun setupFavoriteGamesButtons() {
        if (prefNames.size >= 1) {
            setupGameButton(btnGame1, btnGame1Text, prefNames[0], 1)
            btnGame1.setOnClickListener { if (currentCategory != "game1") { switchCategory("game1", prefNames[0]) } }
        } else {
            btnGame1.visibility = View.GONE
        }

        if (prefNames.size >= 2) {
            setupGameButton(btnGame2, btnGame2Text, prefNames[1], 2)
            btnGame2.setOnClickListener { if (currentCategory != "game2") { switchCategory("game2", prefNames[1]) } }
        } else {
            btnGame2.visibility = View.GONE
        }

        if (prefNames.size >= 3) {
            setupGameButton(btnGame3, btnGame3Text, prefNames[2], 3)
            btnGame3.setOnClickListener { if (currentCategory != "game3") { switchCategory("game3", prefNames[2]) } }
        } else {
            btnGame3.visibility = View.GONE
        }

        updateButtonStyles(currentCategory)
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
            "marvel-rivals" -> "rivals"
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
        resetButtonToInactive(previousCategory)
        currentCategory = newCategory
        updateButtonStyles(newCategory)
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
    private fun getCurrentGame(): String? {
        return when (currentCategory) {
            "game1" -> prefNames.getOrNull(0)
            "game2" -> prefNames.getOrNull(1)
            "game3" -> prefNames.getOrNull(2)
            else -> null
        }
    }
}