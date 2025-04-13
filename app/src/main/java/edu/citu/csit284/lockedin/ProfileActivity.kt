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

class ProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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