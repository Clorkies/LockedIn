package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.util.toast

class ProfileActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val name = findViewById<EditText>(R.id.name)
        val bio = findViewById<EditText>(R.id.bio)
        val pass = findViewById<EditText>(R.id.password)
        val email = findViewById<EditText>(R.id.email)

        val editList  = listOf(name,bio,pass)
        val userInfo = intent.getStringExtra("userInfo")
        toast("User Info: $userInfo")
        users
            .whereEqualTo("email",userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty){
                    for (document in documents) {
                        name.setText(document.getString("username"))
                        pass.setText(document.getString("password"))
                        email.setText(document.getString("email"))
                    }
                } else {
                    users
                        .whereEqualTo("username",userInfo)
                        .get()
                        .addOnSuccessListener { documents ->
                            if(!documents.isEmpty){
                                for (document in documents) {
                                    name.setText(document.getString("username"))
                                    pass.setText(document.getString("password"))
                                    email.setText(document.getString("email"))
                                }
                            }
                        }
                }
            }
        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish(); }

        val btn_settings = findViewById<ImageButton>(R.id.button_settings)
        btn_settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish(); }

        val btn_logout = findViewById<Button>(R.id.button_logout)
        btn_logout.setOnClickListener {
            val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
            val bottom = BottomSheetDialog(this,R.style.BottomSheetDialogTheme)
            bottom.setContentView(sheet)
            val back = sheet.findViewById<Button>(R.id.back_btn)
            back.setOnClickListener {
                bottom.dismiss()
            }
            val logout = sheet.findViewById<Button>(R.id.logout)
            logout.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            bottom.show()
        }
    }
}