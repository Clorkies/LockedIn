package edu.citu.csit284.lockedin

import android.app.Activity
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
        val readMoreSheet = findViewById<LinearLayout>(R.id.sheetReadMore)
        val readMore = findViewById<LinearLayout>(R.id.readMore)

        readMore.setOnClickListener {
            // Enter here action to open the link via webview
        }

        readMoreSheet.translationX = 800f

        readMoreSheet.post {
            readMoreSheet.animate()
                .translationX(280f)
                .setDuration(2250)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .into(imageView)

        titleView.text = title
        textView.text = articleText
    }

}