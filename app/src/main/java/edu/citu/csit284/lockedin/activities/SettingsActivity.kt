package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.toast

class SettingsActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    private lateinit var games: MutableList<Int>
    private var gamesCount: Int = 0
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private lateinit var tv4: TextView
    private lateinit var tv5: TextView
    private lateinit var tv6: TextView
    private lateinit var sw1: SwitchCompat
    private lateinit var sw2: SwitchCompat
    private lateinit var sw3: SwitchCompat
    private lateinit var sw4: SwitchCompat
    private lateinit var sw5: SwitchCompat
    private lateinit var sw6: SwitchCompat
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        tv1 = findViewById(R.id.tv1)
        tv2 = findViewById(R.id.tv2)
        tv3 = findViewById(R.id.tv3)
        tv4 = findViewById(R.id.tv4)
        tv5 = findViewById(R.id.tv5)
        tv6 = findViewById(R.id.tv6)
        sw1 = findViewById(R.id.sw1)
        sw2 = findViewById(R.id.sw2)
        sw3 = findViewById(R.id.sw3)
        sw4 = findViewById(R.id.sw4)
        sw5 = findViewById(R.id.sw5)
        sw6 = findViewById(R.id.sw6)

        findViewById<ImageView>(R.id.button_back).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnDeveloper).setOnClickListener { startActivity(Intent(this, AboutDevActivity::class.java)) }
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        username = sharedPref.getString("username", "")
        username?.let {
            users.whereEqualTo("username", it)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) return@addOnSuccessListener
                    val document = documents.documents[0]
                    val rawFavGames = document.get("favGames") as? List<Long>
                    games = rawFavGames?.map { it.toInt() }?.toMutableList() ?: mutableListOf()
                    gamesCount = games.size
                    setup(games)
                    setupSwitchListeners() // Set up listeners after initial setup
                }
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
            val bottom = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            bottom.setContentView(sheet)
            sheet.findViewById<Button>(R.id.back_btn)?.setOnClickListener { bottom.dismiss() }
            sheet.findViewById<Button>(R.id.logout)?.setOnClickListener {
                getSharedPreferences("User", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            bottom.show()
        }
    }

    private fun setup(games: List<Int>) {
        sw1.isChecked = games.contains(1)
        sw2.isChecked = games.contains(2)
        sw3.isChecked = games.contains(3)
        sw4.isChecked = games.contains(4)
        sw5.isChecked = games.contains(5)
        sw6.isChecked = games.contains(6)
        updateGameUI(1, sw1.isChecked)
        updateGameUI(2, sw2.isChecked)
        updateGameUI(3, sw3.isChecked)
        updateGameUI(4, sw4.isChecked)
        updateGameUI(5, sw5.isChecked)
        updateGameUI(6, sw6.isChecked)
    }

    private fun setupSwitchListeners() {
        sw1.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(1, isChecked) }
        sw2.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(2, isChecked) }
        sw3.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(3, isChecked) }
        sw4.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(4, isChecked) }
        sw5.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(5, isChecked) }
        sw6.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(6, isChecked) }
    }

    private fun onSwitchChanged(gameId: Int, isChecked: Boolean) {
        if (isChecked) {
            if (games.size < 3) {
                if (!games.contains(gameId)) {
                    games.add(gameId)
                    updateFirebaseFavGames()
                    updateGameUI(gameId, true)
                }
            } else {
                if (!games.contains(gameId)) {
                    toast("You can only select up to 3 preferred games")
                    getSwitchForGameId(gameId)?.isChecked = false
                } else {
                    updateGameUI(gameId, true)
                }
            }
        } else {
            if (games.size > 1) {
                games.remove(gameId)
                updateFirebaseFavGames() // Update Firebase on change
                updateGameUI(gameId, false)
            } else {
                toast("You must select atleast 1 favorite game")
                getSwitchForGameId(gameId)?.isChecked = true // Revert the switch state
            }
        }
        gamesCount = games.size // Update gamesCount
    }

    private fun updateGameUI(gameId: Int, isSelected: Boolean) {
        val tv: TextView?
        when (gameId) {
            1 -> tv = tv1
            2 -> tv = tv2
            3 -> tv = tv3
            4 -> tv = tv4
            5 -> tv = tv5
            6 -> tv = tv6
            else -> tv = null
        }
        tv?.setTextColor(ContextCompat.getColor(this, if (isSelected) R.color.yellow else android.R.color.white))
    }

    private fun getSwitchForGameId(gameId: Int): SwitchCompat? {
        return when (gameId) {
            1 -> sw1
            2 -> sw2
            3 -> sw3
            4 -> sw4
            5 -> sw5
            6 -> sw6
            else -> null
        }
    }

    private fun updateFirebaseFavGames() {
        username?.let {
            users.whereEqualTo("username", it)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documentId = documents.documents[0].id
                        users.document(documentId)
                            .update("favGames", games)
                            .addOnSuccessListener {
                            }
                    }
                }

        }
    }
}