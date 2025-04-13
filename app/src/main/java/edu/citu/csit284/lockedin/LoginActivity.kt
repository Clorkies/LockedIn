package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle


class LoginActivity : Activity() {
    private val users = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val sharedPref: SharedPreferences = getSharedPreferences("User", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "")
        val isRemembered = sharedPref.getBoolean("remember", false)


        val loginBottomSheet = findViewById<LinearLayout>(R.id.login_bottom_sheet)
        val logo = findViewById<ImageView>(R.id.logo)
        val welcomeText = findViewById<TextView>(R.id.welcome)
        val checkBox = findViewById<CheckBox>(R.id.remember)

        loginBottomSheet.translationY = 600f
        logo.translationY = -50f
        logo.alpha = 0f
        welcomeText.translationY = -30f
        welcomeText.alpha = 0f

        loginBottomSheet.post {
            loginBottomSheet.animate()
                .translationY(120f)
                .setDuration(850)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        logo.post {
            logo.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        welcomeText.post {
            welcomeText.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)

        if (isRemembered) {
            goNext()
        }
        btnLogin.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()

            if(user.isEmpty() || pass.isEmpty()){
                toast("Please fill out all fields!")
            } else{
                users
                    .whereEqualTo("username",user)
                    .whereEqualTo("password",pass)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(!documents.isEmpty){
                            for (document in documents) {
                                val username = document.getString("username")
                                toast("Welcome, ${username ?: "!"}")
                            }
                            if (checkBox.isChecked) {
                                val editor = sharedPref.edit()
                                editor.putString("username", user)
                                editor.putBoolean("remember", true)
                                editor.apply()
                            } else {
                                sharedPref.edit().clear().apply()
                            }
                            goNext()
                        }
                    }
            }
        }
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        password.toggle(imgPriv)
    }
    private fun goNext(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}