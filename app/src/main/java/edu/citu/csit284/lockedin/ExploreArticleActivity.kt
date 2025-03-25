package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class ExploreArticleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_article)

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

        val imageResource = intent.getIntExtra("imageResource", R.drawable.img_placeholder)
        val title = intent.getStringExtra("title") ?: "Untitled"
        val articleText = intent.getStringExtra("articleText") ?: "No content available."

        val imageView = findViewById<ImageView>(R.id.articleImageView)
        val titleView = findViewById<TextView>(R.id.articleTitleView)
        val textView = findViewById<TextView>(R.id.articleTextView)

        imageView.setImageResource(imageResource)
        titleView.text = title
        textView.text = articleText
    }
}