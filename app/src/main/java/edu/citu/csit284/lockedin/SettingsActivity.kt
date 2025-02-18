package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)) }

        val btn_devs = findViewById<ImageButton>(R.id.button_about_dev)
        btn_devs.setOnClickListener {startActivity(Intent(this, AboutDevActivity::class.java)) }
    }
}