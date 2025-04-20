package edu.citu.csit284.lockedin.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.setupClickableTermsAndPrivacy
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle

class RegisterActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingAnimationContainer: LinearLayout
    private lateinit var loadingCircle: LinearLayout

    private fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(regex))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        setupClickableTermsAndPrivacy(this)

        val btnBack = findViewById<ImageView>(R.id.backBtn)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val emailEditText = findViewById<EditText>(R.id.email)
        val displayNameEditText = findViewById<EditText>(R.id.displayName)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmpass)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        val imgPriv2 = findViewById<ImageView>(R.id.imgPriv2)

        loadingAnimationContainer = findViewById(R.id.loadingAnimationContainer)
        loadingAnimationContainer.visibility = View.GONE
        loadingCircle = findViewById(R.id.loadingCircle)

        val tvPasswordStrength = findViewById<TextView>(R.id.tvPasswordStrength)
        val tvRuleLength = findViewById<TextView>(R.id.tvRuleLength)
        val tvRuleUppercase = findViewById<TextView>(R.id.tvRuleUppercase)
        val tvRuleNumber = findViewById<TextView>(R.id.tvRuleNumber)

        val registerBottomSheet = findViewById<LinearLayout>(R.id.register_bottom_sheet)

        registerBottomSheet.translationY = 800f

        registerBottomSheet.post {
            registerBottomSheet.animate()
                .translationY(25f)
                .setDuration(850)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        btnRegister.isEnabled = false
        btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
        passwordEditText.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                var metRules = 0

                val isLengthValid = pass.length >= 8
                val isGreaterThanMin = pass.length > 8
                val hasUppercase = pass.any { it.isUpperCase() }
                val hasNumber = pass.any { it.isDigit() }

                metRules += updateRuleStatus(tvRuleLength, isLengthValid)
                metRules += updateRuleStatus(tvRuleUppercase, hasUppercase)
                metRules += updateRuleStatus(tvRuleNumber, hasNumber)
                metRules += if (isGreaterThanMin) 1 else 0

                when (metRules) {
                    0, 1 -> {
                        tvPasswordStrength.text = "Weak"
                        btnRegister.isEnabled = false
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.red))
                        btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
                    }
                    2 -> {
                        tvPasswordStrength.text = "Good"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.devYellow))
                        btnRegister.isEnabled = isLengthValid && emailEditText.text.isNotEmpty() && displayNameEditText.text.isNotEmpty() && confirmPasswordEditText.text.toString() == pass
                        if (btnRegister.isEnabled) btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                    3 -> {
                        tvPasswordStrength.text = "Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.teal_700))
                        btnRegister.isEnabled = isLengthValid && emailEditText.text.isNotEmpty() && displayNameEditText.text.isNotEmpty() && confirmPasswordEditText.text.toString() == pass
                        if (btnRegister.isEnabled) btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                    4 -> {
                        tvPasswordStrength.text = "Very Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.purple_500))
                        btnRegister.isEnabled = isGreaterThanMin && emailEditText.text.isNotEmpty() && displayNameEditText.text.isNotEmpty() && confirmPasswordEditText.text.toString() == pass
                        if (btnRegister.isEnabled) btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            private fun updateRuleStatus(textView: TextView, isValid: Boolean): Int {
                val color = if (!isValid) R.color.passwordReqs else R.color.devYellow
                textView.setTextColor(ContextCompat.getColor(this@RegisterActivity, color))
                return if (isValid) 1 else 0
            }
        })

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pass = passwordEditText.text.toString()
                val isLengthValid = pass.length >= 8
                val isGreaterThanMin = pass.length > 8
                btnRegister.isEnabled = (isLengthValid || isGreaterThanMin) && emailEditText.text.isNotEmpty() && displayNameEditText.text.isNotEmpty() && confirmPasswordEditText.text.toString() == pass
                if (btnRegister.isEnabled) btnRegister.setBackgroundResource(R.drawable.btn_register)
                else btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        emailEditText.addTextChangedListener(textWatcher)
        displayNameEditText.addTextChangedListener(textWatcher)
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pass = passwordEditText.text.toString()
                btnRegister.isEnabled = (pass.length >= 8 || pass.length > 8) && emailEditText.text.isNotEmpty() && displayNameEditText.text.isNotEmpty() && s.toString() == pass
                if (btnRegister.isEnabled) btnRegister.setBackgroundResource(R.drawable.btn_register)
                else btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            loadingAnimationContainer.visibility = View.VISIBLE

            val em = emailEditText.text.toString()
            val pass = passwordEditText.text.toString()
            val confpass = confirmPasswordEditText.text.toString()
            val user = displayNameEditText.text.toString()

            if (em.isEmpty() || pass.isEmpty() || confpass.isEmpty() || user.isEmpty()) {
                toast("Please fill out all fields!")
                return@setOnClickListener
            }

            if (!isValidEmail(em)) {
                toast("Please enter a valid email!")
                return@setOnClickListener
            }

            if (pass != confpass) {
                toast("Passwords do not match!")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(em, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            users.whereEqualTo("username", user)
                                .get()
                                .addOnSuccessListener { duplicates ->
                                    if (!duplicates.isEmpty) {
                                        firebaseUser.delete()
                                            .addOnCompleteListener { deleteTask ->
                                                if (deleteTask.isSuccessful) {
                                                    toast("Username already exists! Please try another")
                                                } else {
                                                    toast("Username exists, but failed to clean up auth account.")
                                                }
                                            }
                                    } else {
                                        val userMap = hashMapOf(
                                            "uid" to firebaseUser.uid,
                                            "email" to em,
                                            "username" to user
                                        )
                                        users.document(firebaseUser.uid)
                                            .set(userMap)
                                            .addOnSuccessListener {
                                                loadingAnimationContainer.visibility = View.GONE
                                                toast("Registered Successfully!")
                                                val intent = Intent(this, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                loadingAnimationContainer.visibility = View.GONE
                                                toast("Failed to save user data.")
                                                firebaseUser.delete()
                                                    .addOnCompleteListener {  }
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    toast("Error checking username.")
                                    firebaseUser.delete()
                                        .addOnCompleteListener {  }
                                }
                        }
                    } else {
                        toast("Registration failed: ${task.exception?.message}")
                    }
                }
        }
        passwordEditText.toggle(imgPriv)
        confirmPasswordEditText.toggle(imgPriv2)
    }
}

