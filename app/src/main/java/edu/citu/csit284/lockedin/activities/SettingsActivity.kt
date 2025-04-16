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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R

class SettingsActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    private lateinit var games : List<Int>
    private var gamesCount : Int = 0
    private lateinit var tv1 : TextView
    private lateinit var tv2 : TextView
    private lateinit var tv3 : TextView
    private lateinit var tv4 : TextView
    private lateinit var tv5 : TextView
    private lateinit var tv6 : TextView
    private lateinit var sw1: SwitchCompat
    private lateinit var sw2: SwitchCompat
    private lateinit var sw3: SwitchCompat
    private lateinit var sw4: SwitchCompat
    private lateinit var sw5: SwitchCompat
    private lateinit var sw6: SwitchCompat
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

        findViewById<ImageView>(R.id.button_back).setOnClickListener { finish()}

        findViewById<Button>(R.id.btnDeveloper).setOnClickListener { startActivity(Intent(this, AboutDevActivity::class.java)) }
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val username = sharedPref.getString("username","")
        users.whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener
                val document = documents.documents[0]
                val rawFavGames = document.get("favGames") as? List<Long>
                games = rawFavGames?.map { it.toInt() } ?: emptyList()
                gamesCount = games.size
            }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
            val bottom = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

            bottom.setContentView(sheet)

            val back = sheet.findViewById<Button>(R.id.back_btn)
            back.setOnClickListener {
                bottom.dismiss()
            }
            val logout = sheet.findViewById<Button>(R.id.logout)
            logout.setOnClickListener {
                getSharedPreferences("User", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            bottom.show()
        }


    }
}