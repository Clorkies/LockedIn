package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.splash.LoginSplashScreen
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle

class LoginActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val sharedPref: SharedPreferences = getSharedPreferences("User", MODE_PRIVATE)
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
                .translationY(100f)
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

        val emailEditText = findViewById<EditText>(R.id.email) // Changed to email
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)

        if (isRemembered) {
            goNext()
        }
        btnLogin.setOnClickListener {
            val email = emailEditText.text.toString().trim() // Use email
            val pass = password.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                toast("Please fill out all fields!")
            } else {
                btnLogin.isEnabled = false
                btnLogin.text = "Logging in..."

                Log.d("LoginActivity", "Attempting to login with email: $email") // Log email

                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                // Get the username from Firestore using the user's UID
                                users.document(firebaseUser.uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val username = document.getString("username")
                                            val editor = sharedPref.edit()
                                            if (checkBox.isChecked) {
                                                editor.putString("username", username) // Store username
                                                editor.putBoolean("remember", true)
                                                editor.apply()
                                            } else {
                                                editor.putString("username", username)  // Store username
                                                editor.putBoolean("remember", false)
                                                editor.apply()
                                            }
                                            goNext()
                                        } else {
                                            toast("Error: Username not found in Firestore.")
                                            btnLogin.isEnabled = true
                                            btnLogin.text = "Login"
                                            auth.signOut() // Sign out the user
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("LoginActivity", "Error fetching username from Firestore", e)
                                        toast("Login failed: ${e.localizedMessage}")
                                        btnLogin.isEnabled = true
                                        btnLogin.text = "Login"
                                        auth.signOut()
                                    }
                            }

                        } else {
                            Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                            toast("Invalid email or password")
                            btnLogin.isEnabled = true
                            btnLogin.text = "Login"
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

    private fun goNext() {
        val intent = Intent(this, LoginSplashScreen::class.java)
        startActivity(intent)
        finish()
    }
}

