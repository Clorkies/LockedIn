package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ExploreArticleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_article)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }

        val imageUrl = intent.getStringExtra("imageUrl")
        val title = intent.getStringExtra("title") ?: "Untitled"
        val publishedDate = intent.getStringExtra("date") ?: "00/00/0000"
        val articleUrl = intent.getStringExtra("articleUrl")
        val articleText = intent.getStringExtra("articleText") ?: "No content available."

        val imageView = findViewById<ImageView>(R.id.articleImageView)
        val titleView = findViewById<TextView>(R.id.articleTitleView)
        val textView = findViewById<TextView>(R.id.articleTextView)
        val dateView = findViewById<TextView>(R.id.articleDateView)
        val readMore = findViewById<LinearLayout>(R.id.readMore)

        if (!articleUrl.isNullOrEmpty()) {
            readMore.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(this, R.color.blue))
                    .setStartAnimations(this, R.anim.slide_in_up, R.anim.slide_out_down)
                    .setExitAnimations(this, R.anim.slide_in_down, R.anim.slide_out_up)
                    .build()

                customTabsIntent.launchUrl(this, Uri.parse(articleUrl))
            }
            readMore.visibility = View.VISIBLE
        } else {
            readMore.visibility = View.GONE
        }

        if (!publishedDate.isNullOrEmpty()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(publishedDate)
                date?.let {
                    dateView.text = "Published on ${outputFormat.format(it)}"
                    dateView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                dateView.text = "Published on $publishedDate"
                dateView.visibility = View.VISIBLE
            }
        } else {
            dateView.visibility = View.GONE
        }

        val readMoreSheet = findViewById<LinearLayout>(R.id.sheetReadMore)
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