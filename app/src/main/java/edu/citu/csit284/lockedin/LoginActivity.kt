package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle


class LoginActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBottomSheet = findViewById<LinearLayout>(R.id.login_bottom_sheet)
        val logo = findViewById<ImageView>(R.id.logo)
        val welcomeText = findViewById<TextView>(R.id.welcome)

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

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        btnLogin.setOnClickListener {
            val mail = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if(mail.isEmpty() || pass.isEmpty()){
                toast("Please fill out all fields!")
            } else if ( mail == "admin" && pass == "adminpass") {
                debugLogin()
            } else{

                users
                    .whereEqualTo("email",mail)
                    .whereEqualTo("password",pass)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(!documents.isEmpty){
                            for (document in documents) {
                                val username = document.getString("username")
                                toast("Welcome, ${username ?: "!"}")
                            }
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            toast("Wrong email or password!")
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

    // For debugging purposes
    fun debugLogin() {
        toast("Welcome, admin!")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}