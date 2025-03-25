package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class LandingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val btn_profile = findViewById<ImageButton>(R.id.button_profile)
        btn_profile.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)) }

        val btn_games = findViewById<ImageButton>(R.id.button_games)
        btn_games.setOnClickListener {startActivity(Intent(this, OngoingTournamentsActivity::class.java)) }

        val btn_live = findViewById<ImageButton>(R.id.button_live)
        btn_live.setOnClickListener {startActivity(Intent(this, LiveActivity::class.java)) }

        val btn_explore = findViewById<ImageButton>(R.id.button_explore)
        btn_explore.setOnClickListener {startActivity(Intent(this, ExploreActivity::class.java)) }


    }
}