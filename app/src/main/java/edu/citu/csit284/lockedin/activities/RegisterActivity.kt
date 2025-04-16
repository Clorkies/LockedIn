package edu.citu.csit284.lockedin.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle

class RegisterActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    private fun isValidEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

        return email.matches(Regex(regex))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack = findViewById<ImageView>(R.id.backBtn)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val email = findViewById<EditText>(R.id.email)
        val displayName = findViewById<EditText>(R.id.displayName)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmpass)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        val imgPriv2 = findViewById<ImageView>(R.id.imgPriv2)

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
        password.addTextChangedListener(object : TextWatcher {
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
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity,
                            R.color.red
                        ))
                        btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
                    }
                    2 -> {
                        tvPasswordStrength.text = "Good"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity,
                            R.color.devYellow
                        ))
                        btnRegister.isEnabled = isLengthValid
                        if (isLengthValid) btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                    3 -> {
                        tvPasswordStrength.text = "Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity,
                            R.color.teal_700
                        ))
                        btnRegister.isEnabled = isLengthValid
                        if (isLengthValid) btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                    4 -> {
                        tvPasswordStrength.text = "Very Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity,
                            R.color.purple_500
                        ))
                        btnRegister.isEnabled = isGreaterThanMin
                        if (isGreaterThanMin) btnRegister.setBackgroundResource(R.drawable.btn_register)
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

        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            val em = email.text.toString()
            val pass = password.text.toString()
            val confpass = confirmPassword.text.toString()
            val user = displayName.text.toString()
            if(em == "" || pass == "" || confpass == "" || user == ""){
                toast("Please fill out all fields!")
            }else{
                if(!isValidEmail(em)){
                    toast("Please enter a valid email!")
                }else{

                    if(pass != confpass){
                        toast("Passwords do not match!")
                    }else{
                        users
                            .whereEqualTo("email",em)
                            .get()
                            .addOnSuccessListener { duplicates ->
                                if(!duplicates.isEmpty){
                                    toast("Email already exists! Please try another")
                                }else{
                                    users
                                        .whereEqualTo("username",user)
                                        .get()
                                        .addOnSuccessListener{ duplis ->
                                            if(!duplis.isEmpty){
                                                toast("Username already exists! Please try another")
                                            }else{
                                                val user = hashMapOf(
                                                    "email" to em,
                                                    "username" to user,
                                                    "password" to pass
                                                )
                                                users
                                                    .add(user)
                                                    .addOnSuccessListener {
                                                        toast("Registered Successfully!")
                                                        val intent = Intent(this, LoginActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                                    .addOnFailureListener{
                                                        toast("Failed to register")
                                                    }
                                            }
                                        }

                                }
                            }
                    }
                }
            }
        }
        password.toggle(imgPriv)
        confirmPassword.toggle(imgPriv2)
    }
}