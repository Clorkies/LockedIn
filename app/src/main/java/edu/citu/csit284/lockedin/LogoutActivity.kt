package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class LogoutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)) }

    }
}