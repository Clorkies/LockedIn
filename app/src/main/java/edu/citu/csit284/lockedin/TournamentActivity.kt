package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TournamentActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)
        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener {startActivity(Intent(this, OngoingTournamentsActivity::class.java)) }

        val imageResource = intent.getIntExtra("imageResource", R.drawable.img_placeholder)
        val title = intent.getStringExtra("title") ?: "Untitled"
        val articleText = intent.getStringExtra("articleText") ?: "No content available."

        val header = findViewById<ImageView>(R.id.imgHeader)
        val name = findViewById<TextView>(R.id.tvTitle)
        val desc = findViewById<TextView>(R.id.tvDesc)

        header.setImageResource(imageResource)
        name.text = title
        desc.text = articleText

        val lvList = findViewById<ListView>(R.id.lvList)
        val playerList = listOf("Goat","Goat1","Goat2","Goat3","Goat4","Goat5")


        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, playerList)
        lvList.adapter = adapter

    }
}