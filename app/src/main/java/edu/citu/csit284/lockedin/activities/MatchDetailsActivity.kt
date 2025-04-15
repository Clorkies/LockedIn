package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import edu.citu.csit284.lockedin.R

class MatchDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }


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