package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import edu.citu.csit284.lockedin.data.Tournament
import edu.citu.csit284.lockedin.helper.TournamentCustomListView

class LiveActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        val lvTournaments = findViewById<ListView>(R.id.lvTournaments)
        val tournaments = listOf(
            Tournament(R.drawable.tourney_sample1, "Title 1",
                getString(R.string.lorem),
                "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample2, "Title 2",
                getString(R.string.lorem),
            "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample3, "Title 3",
                getString(R.string.lorem),
            "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample1, "Title 4",
            getString(R.string.lorem),
            "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample2, "Title 5",
            getString(R.string.lorem),
            "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample3, "Title 6",
            getString(R.string.lorem),
            "2025-xx-xx")
        )

        lvTournaments.adapter = TournamentCustomListView(
            this,
            tournaments,
            onClick = {
                startActivity (
                    Intent(this, TournamentActivity::class.java).apply {
                        putExtra("imageResource", it.imgResId)
                        putExtra("title", it.name)
                        putExtra("articleText", it.description)
                        putExtra("caller","live")
                    }
                )
            }
        )

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

        val btn_profile = findViewById<ImageButton>(R.id.button_profile)
        btn_profile.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)) }

        val btn_home = findViewById<ImageButton>(R.id.button_home)
        btn_home.setOnClickListener {startActivity(Intent(this, LandingActivity::class.java)) }

        val btn_games = findViewById<ImageButton>(R.id.button_games)
        btn_games.setOnClickListener {startActivity(Intent(this, GamesActivity::class.java)) }

        val btn_explore = findViewById<ImageButton>(R.id.button_explore)
        btn_explore.setOnClickListener {startActivity(Intent(this, ExploreActivity::class.java)) }

    }
}