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

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener { startActivity(Intent(this, LandingActivity::class.java)) }

        val btn_settings = findViewById<ImageButton>(R.id.button_settings)
        btn_settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        val btn_logout = findViewById<ImageButton>(R.id.button_logout)
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

        val tvName = findViewById<TextView>(R.id.tv_name)
        val btnEditName = findViewById<ImageButton>(R.id.button_edit_name)
        val tvUsername = findViewById<TextView>(R.id.tv_username)
        val btnEditUsername = findViewById<ImageButton>(R.id.button_edit_username)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val btnEditEmail = findViewById<ImageButton>(R.id.button_edit_email)
        setUpEditButton(btnEditName, tvName, "Enter your new name", "Full Name")
        setUpEditButton(btnEditUsername, tvUsername, "Enter your new username", "Username")
        setUpEditButton(btnEditEmail, tvEmail, "Enter your new email", "Email Address")
    }

    fun setUpEditButton(editButton: ImageButton, textView: TextView, title: String, hintText: String ) {
        editButton.setOnClickListener {
            val inputField = EditText(this)
            inputField.setText(textView.text.toString())
            inputField.hint = hintText

            val dialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setView(inputField)
                .setPositiveButton("OK") { _, _ -> textView.text = inputField.text.toString() }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }
}