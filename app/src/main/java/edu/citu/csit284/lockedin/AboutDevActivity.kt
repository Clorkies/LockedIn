package edu.citu.csit284.lockedin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class AboutDevActivity : Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_dev)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        val imgDev = findViewById<ImageView>(R.id.imgDev)
        val name = findViewById<TextView>(R.id.name)
        val hobby = findViewById<TextView>(R.id.hobby)
        val fact = findViewById<TextView>(R.id.fact)
        val clarkImg = findViewById<ImageView>(R.id.Clark)
        val jervImg = findViewById<ImageView>(R.id.Jervin)
        val clarkName = findViewById<TextView>(R.id.clarkName)
        val jervName = findViewById<TextView>(R.id.jervName)

        val origName = name.text
        val origHobby = hobby.text
        val origFact = fact.text

        clarkImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()

        jervImg.setOnClickListener {
            imgDev.setImageResource(R.drawable.imgdev2square)
            name.text = "Jervin Milleza"
            hobby.text = "Trades casually"
            fact.text = "6'4 and nonchalant"
            clarkName.setTextColor(ContextCompat.getColor(this, R.color.white))
            jervName.setTextColor(ContextCompat.getColor(this, R.color.yellow))
            jervImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            clarkImg.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }
        clarkImg.setOnClickListener {
            imgDev.setImageResource(R.drawable.imgdev1square)
            name.text = origName
            hobby.text = origHobby
            fact.text = origFact
            clarkName.setTextColor(ContextCompat.getColor(this, R.color.yellow))
            jervName.setTextColor(ContextCompat.getColor(this, R.color.white))
            clarkImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            jervImg.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }
    }
}