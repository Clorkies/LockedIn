package edu.citu.csit284.lockedin

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class ExploreArticleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_article)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }

        val imageUrl = intent.getStringExtra("imageUrl")
        val title = intent.getStringExtra("title") ?: "Untitled"
        val articleText = intent.getStringExtra("articleText") ?: "No content available."

        val imageView = findViewById<ImageView>(R.id.articleImageView)
        val titleView = findViewById<TextView>(R.id.articleTitleView)
        val textView = findViewById<TextView>(R.id.articleTextView)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .into(imageView)

        titleView.text = title
        textView.text = articleText
    }

}