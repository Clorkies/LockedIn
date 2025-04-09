package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        btnLogin.setOnClickListener {
            val mail = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if(mail.isEmpty() || pass.isEmpty()){
                toast("Please fill out all fields!")
            }else{
                users
                    .whereEqualTo("email",mail)
                    .whereEqualTo("password",pass)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(!documents.isEmpty){
                            toast("Login Successful!")
                            val intent = Intent(this, LandingActivity::class.java)
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
}