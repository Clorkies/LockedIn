package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton

class GamesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        val btn_landing = findViewById<ImageButton>(R.id.button_home)
        btn_landing.setOnClickListener {startActivity(Intent(this, LandingActivity::class.java)) }

        val btn_profile = findViewById<ImageButton>(R.id.button_profile)
        btn_profile.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)).apply {
            intent.putExtra("caller", "game")
        } }

        val btn_games = findViewById<ImageButton>(R.id.button_games)
        btn_games.setOnClickListener {startActivity(Intent(this, GamesActivity::class.java)) }

        val btn_live = findViewById<ImageButton>(R.id.button_live)
        btn_live.setOnClickListener {startActivity(Intent(this, LiveActivity::class.java)) }

        val btn_explore = findViewById<ImageButton>(R.id.button_explore)
        btn_explore.setOnClickListener {startActivity(Intent(this, ExploreActivity::class.java)) }

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

        val matchView1 = findViewById<FrameLayout>(R.id.ongoingMatch1)
        val matchView2 = findViewById<FrameLayout>(R.id.ongoingMatch2)

        matchView1.setOnClickListener {startActivity(Intent(this, MatchDetailsActivity::class.java)).apply {
            intent.putExtra("caller", "game")
        } }

        matchView2.setOnClickListener {startActivity(Intent(this, MatchDetailsActivity::class.java)).apply {
            intent.putExtra("caller", "game")
        } }


    }
}