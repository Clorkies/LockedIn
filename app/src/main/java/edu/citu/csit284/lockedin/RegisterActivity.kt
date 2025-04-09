package edu.citu.csit284.lockedin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle

class RegisterActivity : Activity() {
    fun isValidEmail(email: String): Boolean {
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

        btnRegister.isEnabled = false
        btnRegister.setBackgroundResource(R.drawable.btn_register_disabled)
        password.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                var metRules = 0

                val isLengthValid = pass.length >= 8
                val hasUppercase = pass.any { it.isUpperCase() }
                val hasNumber = pass.any { it.isDigit() }

                metRules += updateRuleStatus(tvRuleLength, isLengthValid)
                metRules += updateRuleStatus(tvRuleUppercase, hasUppercase)
                metRules += updateRuleStatus(tvRuleNumber, hasNumber)

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
                        btnRegister.isEnabled = true
                        btnRegister.setBackgroundResource(R.drawable.btn_register)
                    }
                    3 -> {
                        tvPasswordStrength.text = "Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.teal_700))
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
            val username = displayName.text.toString()
            if(em == "" || pass == "" || confpass == "" || username == ""){
                toast("Please fill out all fields!")
            }else{
                if(!isValidEmail(em)){
                    toast("Please enter a valid email!")
                }else{
                    if(pass != confpass){
                        toast("Passwords do not match!")
                    }else{
                        val user = hashMapOf(
                            "email" to em,
                            "username" to username,
                            "password" to pass
                        )
                        Firebase.firestore.collection("users")
                            .add(user)
                            .addOnSuccessListener {
                                toast("Registered Successfully!")
                            }
                            .addOnFailureListener{
                                toast("Failed to register")
                            }
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
        password.toggle(imgPriv)
        confirmPassword.toggle(imgPriv2)
    }
}