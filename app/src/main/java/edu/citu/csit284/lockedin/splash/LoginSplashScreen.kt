package edu.citu.csit284.lockedin.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.MainActivity
import edu.citu.csit284.lockedin.R

class LoginSplashScreen : Activity() {
    private val users = Firebase.firestore.collection("users")
    private lateinit var customizeSheet: LinearLayout
    private lateinit var imgAndProfile: LinearLayout
    private val selectedGames = mutableSetOf<Int>()
    private val MAX_SELECTIONS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_splash_screen)

        val imgpfp = findViewById<ImageView>(R.id.pfp)
        val bgImg = findViewById<ImageView>(R.id.bgImage)
        val name = findViewById<TextView>(R.id.name)
        val welcome = findViewById<TextView>(R.id.welcome)
        customizeSheet = findViewById(R.id.customizationSheet)
        imgAndProfile = findViewById(R.id.imgAndProfile)
        val selectionCountText = findViewById<TextView>(R.id.selection_count)
        val btnProceed = findViewById<Button>(R.id.btn_proceed)
        btnProceed.background = ContextCompat.getDrawable(this, R.drawable.btn_register_disabled)

        customizeSheet.visibility = View.GONE

        imgAndProfile.visibility = View.VISIBLE

        imgpfp.visibility = View.INVISIBLE
        name.visibility = View.INVISIBLE
        welcome.visibility = View.INVISIBLE

        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val userInfo = sharedPref.getString("username", "")
        name.text = userInfo
        val fade = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        bgImg.startAnimation(fade)

        setupGameCardSelection(selectionCountText, btnProceed)

        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val pfp = document.getLong("pfpID")?.toInt() ?: 2
                    when (pfp) {
                        1 -> {
                            imgpfp.setImageResource(R.drawable.red_pfp)
                            name.setTextColor(ContextCompat.getColor(this, R.color.red))
                        }
                        2 -> {
                            imgpfp.setImageResource(R.drawable.default_pfp)
                            name.setTextColor(ContextCompat.getColor(this, R.color.yellow))
                        }
                        3 -> {
                            imgpfp.setImageResource(R.drawable.green_pfp)
                            name.setTextColor(ContextCompat.getColor(this, R.color.green))
                        }
                        4 -> {
                            imgpfp.setImageResource(R.drawable.blue_pfp)
                            name.setTextColor(ContextCompat.getColor(this, R.color.pfpblue))
                        }
                    }

                    val favGames = document.get("favGames") as? ArrayList<Long>

                    if (favGames == null) {
                        showCustomizationSheet()
                    } else {
                        proceedToMainActivity(2500)
                    }
                }

                imgpfp.visibility = View.VISIBLE
                name.visibility = View.VISIBLE
                welcome.visibility = View.VISIBLE

                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
                val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_fade_up)

                imgpfp.startAnimation(fadeIn)
                name.startAnimation(slideUp)
                welcome.startAnimation(slideUp)
            }

        btnProceed.setOnClickListener {
            if (selectedGames.size == MAX_SELECTIONS) {
                saveUserGameSelection()
            }
        }
    }

    private fun setupGameCardSelection(selectionCountText: TextView, btnProceed: Button) {
        val gameCards = listOf(
            Pair(1, findViewById<FrameLayout>(R.id.card_valorant_frame)),
            Pair(2, findViewById<FrameLayout>(R.id.card_league_frame)),
            Pair(3, findViewById<FrameLayout>(R.id.card_csgo_frame)),
            Pair(4, findViewById<FrameLayout>(R.id.card_dota_frame)),
            Pair(5, findViewById<FrameLayout>(R.id.card_rivals_frame)),
            Pair(6, findViewById<FrameLayout>(R.id.card_overwatch_frame))
        )

        gameCards.forEach { (gameId, cardFrame) ->
            val checkIcon = cardFrame.findViewById<ImageView>("check_${getGameNameById(gameId)}".let { resources.getIdentifier(it, "id", packageName) })

            cardFrame.setOnClickListener {
                toggleGameSelection(gameId, checkIcon)
                updateSelectionCount(selectionCountText)
                updateProceedButtonState(btnProceed)
            }
        }
    }

    private fun getGameNameById(id: Int): String {
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

    private fun toggleGameSelection(gameId: Int, checkIcon: ImageView) {
        if (selectedGames.contains(gameId)) {
            selectedGames.remove(gameId)
            checkIcon.visibility = View.GONE

            val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)
            checkIcon.startAnimation(scaleDown)
        } else {
            if (selectedGames.size < MAX_SELECTIONS) {
                selectedGames.add(gameId)
                checkIcon.visibility = View.VISIBLE

                val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
                checkIcon.startAnimation(scaleUp)
            } else {
                Toast.makeText(this, "You can only select $MAX_SELECTIONS games", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectionCount(textView: TextView) {
        textView.text = "${selectedGames.size}/$MAX_SELECTIONS selected"
    }

    private fun updateProceedButtonState(button: Button) {
        button.isEnabled = selectedGames.size == MAX_SELECTIONS
        if (button.isEnabled) {
            button.background = ContextCompat.getDrawable(this, R.drawable.btn_register)
        } else {
            button.background = ContextCompat.getDrawable(this, R.drawable.btn_register_disabled)
        }
    }

    private fun showCustomizationSheet() {
        imgAndProfile.visibility = View.GONE

        customizeSheet.visibility = View.VISIBLE
        customizeSheet.translationY = 2000f

        val slideUpAnimation = ObjectAnimator.ofFloat(customizeSheet, "translationY", 2000f, 650f)
        slideUpAnimation.duration = 800
        slideUpAnimation.interpolator = DecelerateInterpolator()
        slideUpAnimation.start()
    }

    private fun saveUserGameSelection() {
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val userInfo = sharedPref.getString("username", "")

        users.whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val docRef = document.reference

                    val favGames = selectedGames.toList().sorted()

                    docRef.update("favGames", favGames)
                        .addOnSuccessListener {
                            val slideDownAnimation = ObjectAnimator.ofFloat(customizeSheet, "translationY", 300f, 2000f)
                            slideDownAnimation.duration = 500
                            slideDownAnimation.interpolator = AccelerateInterpolator()
                            slideDownAnimation.start()

                            slideDownAnimation.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    customizeSheet.visibility = View.GONE
                                    imgAndProfile.visibility = View.VISIBLE
                                    proceedToMainActivity(800)
                                }
                            })
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save preferences: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun proceedToMainActivity(delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, delay)
    }
}