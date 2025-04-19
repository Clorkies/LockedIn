package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Match
import edu.citu.csit284.lockedin.helper.LiveMatchAdapter
import java.util.Locale

class MatchDetailsActivity : Activity() {

    private lateinit var logoGame: ImageView
    private lateinit var gameName: TextView
    private lateinit var league: TextView
    private lateinit var serie: TextView
    private lateinit var tournament: TextView
    private lateinit var imgTeam1: ImageView
    private lateinit var team1: TextView
    private lateinit var team1Score: TextView
    private lateinit var imgTeam2: ImageView
    private lateinit var team2: TextView
    private lateinit var team2Score: TextView
    private lateinit var watchButton: LinearLayout

    private var currentMatch: Match? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_details)

        logoGame = findViewById(R.id.logoGame)
        gameName = findViewById(R.id.gameName)
        league = findViewById(R.id.league)
        serie = findViewById(R.id.serie)
        tournament = findViewById(R.id.tournament)
        imgTeam1 = findViewById(R.id.imgTeam1)
        team1 = findViewById(R.id.team1)
        team1Score = findViewById(R.id.team1Score)
        imgTeam2 = findViewById(R.id.imgTeam2)
        team2 = findViewById(R.id.team2)
        team2Score = findViewById(R.id.team2Score)
        watchButton = findViewById(R.id.watchButton)
        watchButton.translationX = 800f

        watchButton.post {
            watchButton.animate()
                .translationX(280f)
                .setDuration(1850)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }

        val matchJson = intent.getStringExtra(LiveMatchAdapter.EXTRA_MATCH)
        val gson = Gson()
        currentMatch = gson.fromJson(matchJson, Match::class.java)

        currentMatch?.let { match ->
            gameName.text = match.videogame.name

            league.text = match.league?.name ?: "N/A"
            serie.text = match.serie?.full_name ?: "N/A"
            tournament.text = match.tournament?.name ?: "N/A"

            if (match.opponents.size >= 2) {
                Glide.with(this)
                    .load(match.opponents[0].opponent.image_url)
                    .placeholder(R.drawable.default_pfp)
                    .error(R.drawable.default_pfp)
                    .into(imgTeam1)
                team1.text = match.opponents[0].opponent.name
                Glide.with(this)
                    .load(match.opponents[1].opponent.image_url)
                    .placeholder(R.drawable.default_pfp)
                    .error(R.drawable.default_pfp)
                    .into(imgTeam2)
                team2.text = match.opponents[1].opponent.name
            } else {
                team1.text = match.opponents.getOrNull(0)?.opponent?.name ?: "TBD"
                team2.text = match.opponents.getOrNull(1)?.opponent?.name ?: "TBD"
                imgTeam1.setImageResource(R.drawable.default_pfp)
                imgTeam2.setImageResource(R.drawable.red_pfp)
            }

            if (match.results.isNotEmpty()) {
                team1Score.text = match.results[0].score.toString()
                team2Score.text = match.results[1].score.toString()
            } else {
                team1Score.text = "-"
                team2Score.text = "-"
            }

            if (match.streams_list.isNotEmpty()) {
                watchButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(match.streams_list[0].raw_url))
                    startActivity(intent)
                }
            } else {
                watchButton.isEnabled = false
            }

            val logoName = when (match.videogame.name) {
                "Valorant" -> "valorant"
                "LoL" -> "league"
                "Counter-Strike" -> "csgo"
                "Dota 2" -> "dota"
                "Mobile Legends: Bang Bang" -> "mlbb"
                "Overwatch" -> "overwatch"
                else -> ""
            }
            if (logoName.isNotEmpty()) {
                val resourceId = resources.getIdentifier("logo_$logoName", "drawable", packageName)
                if (resourceId != 0) {
                    Glide.with(this)
                        .load(resourceId)
                        .error(R.drawable.logo_valorant)
                        .into(logoGame)
                } else {
                    Glide.with(this)
                        .load(R.drawable.logo_valorant)
                        .into(logoGame)
                }
            } else {
                Glide.with(this)
                    .load(R.drawable.logo_valorant)
                    .into(logoGame)
            }
        }
    }
}