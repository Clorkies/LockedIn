package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog

class ProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener { startActivity(Intent(this, LandingActivity::class.java)) }

        val btn_settings = findViewById<ImageButton>(R.id.button_settings)
        btn_settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        val tvName = findViewById<TextView>(R.id.tv_name)
        val btnEditName = findViewById<ImageButton>(R.id.button_edit_name)
        btnEditName.setOnClickListener {
            val inputName = EditText(this)
            inputName.setText(tvName.text.toString())

            val dialog = AlertDialog.Builder(this)
                .setTitle("Enter your new name")
                .setView(inputName)
                .setPositiveButton("OK") { _, _ -> tvName.text = inputName.text.toString() }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }
}
