package edu.citu.csit284.lockedin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingsActivity : Activity() {
    private fun diaLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val btnDeveloper = findViewById<Button>(R.id.btnDeveloper)
        btnDeveloper.setOnClickListener {
            val intent = Intent(this, AboutDevActivity::class.java)
            startActivity(intent)
        }
        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("caller", "landing")
            }
            startActivity(intent)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
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

        val switchActive: SwitchCompat = findViewById(R.id.switchActive)
        val switchNoti: SwitchCompat = findViewById(R.id.switchNoti)
        val switchPriv: SwitchCompat = findViewById(R.id.switchPriv)

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Active Status on", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Active Status off", Toast.LENGTH_SHORT).show()
            }
        }
        switchNoti.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Notifications on", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Notifications o+f", Toast.LENGTH_SHORT).show()
            }
        }
        switchPriv.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Account is Private", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Account is Public", Toast.LENGTH_SHORT).show()
            }
        }
        val secpriv: RelativeLayout = findViewById(R.id.secpriv)
        val access: RelativeLayout = findViewById(R.id.access)
        val faq: RelativeLayout = findViewById(R.id.faq)
        secpriv.setOnClickListener {
            Toast.makeText(this, "Security and Privacy Screen TBD", Toast.LENGTH_SHORT).show()
        }
        access.setOnClickListener {
            Toast.makeText(this, "Accessibility Screen TBD", Toast.LENGTH_SHORT).show()
        }
        faq.setOnClickListener {
            Toast.makeText(this, "FAQ and About Us Screen TBD", Toast.LENGTH_SHORT).show()
        }
    }
}