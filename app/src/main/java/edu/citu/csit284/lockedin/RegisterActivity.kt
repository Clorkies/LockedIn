package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmpass)
        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            val em = email.text.toString()
            val pass = password.text.toString()
            val confpass = confirmPassword.text.toString()
            if(em == "" || pass == "" || confpass == ""){
                Toast.makeText(this,"Please fill out all fields!",Toast.LENGTH_SHORT).show()
            }else{
                if(!isValidEmail(em)){
                    Toast.makeText(this,"Please enter a valid email!",Toast.LENGTH_SHORT).show()
                }else{
                    if(pass != confpass){
                        Toast.makeText(this,"Passwords do not match!",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Registered Successfully!",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

    }
}