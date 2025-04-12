package edu.citu.csit284.lockedin

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class ExploreArticleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_article)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }

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