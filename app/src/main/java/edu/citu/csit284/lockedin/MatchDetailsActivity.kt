package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView

class MatchDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)

        val caller = intent.getStringExtra("caller")

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener {
            when (caller) {
                "landing" -> startActivity(Intent(this, LandingActivity::class.java))
                "game" -> startActivity(Intent(this, GamesActivity::class.java))
                "live" -> startActivity(Intent(this, LiveActivity::class.java))
                "explore" -> startActivity(Intent(this, ExploreActivity::class.java))
                else -> finish()
            }
        }

        val playerList = listOf("TenZ","Zombs","ShahZam","Zekken","Asuna","Yay","Grim","Zombs","ShahZam","Zekken","Asuna","Yay","Grim"
            ,"Zombs","ShahZam","Zekken","Asuna","Yay","Grim","Zombs","ShahZam","Zekken","Asuna","Yay","Grim","Zombs","ShahZam","Zekken","Asuna","Yay","Grim")

        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            playerList
        )

        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = arrayAdapter

        val btn_watch_live = findViewById<Button>(R.id.buttonWatchLive)
        btn_watch_live.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.twitch.tv")

            startActivity(intent)
        }
    }
}