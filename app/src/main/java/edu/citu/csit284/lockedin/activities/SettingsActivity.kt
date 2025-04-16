package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import edu.citu.csit284.lockedin.R

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.backBtn).setOnClickListener { finish()}

        findViewById<Button>(R.id.btnDeveloper).setOnClickListener { startActivity(Intent(this, AboutDevActivity::class.java)) }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
            val bottom = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

            bottom.setContentView(sheet)

            val back = sheet.findViewById<Button>(R.id.back_btn)
            back.setOnClickListener {
                bottom.dismiss()
            }
            val logout = sheet.findViewById<Button>(R.id.logout)
            logout.setOnClickListener {
                getSharedPreferences("User", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
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