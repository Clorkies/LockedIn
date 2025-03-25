package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import edu.citu.csit284.lockedin.data.Article
import edu.citu.csit284.lockedin.helper.ArticleCustomListView

class LandingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val btn_landing = findViewById<ImageButton>(R.id.button_home)
        btn_landing.setOnClickListener {startActivity(Intent(this, LandingActivity::class.java)) }

        val btn_profile = findViewById<ImageButton>(R.id.button_profile)
        btn_profile.setOnClickListener {startActivity(Intent(this, ProfileActivity::class.java)).apply {
            intent.putExtra("caller", "landing")
        } }

        val btn_games = findViewById<ImageButton>(R.id.button_games)
        btn_games.setOnClickListener {startActivity(Intent(this, GamesActivity::class.java)) }

        val btn_live = findViewById<ImageButton>(R.id.button_live)
        btn_live.setOnClickListener {startActivity(Intent(this, LiveActivity::class.java)) }

        val btn_explore = findViewById<ImageButton>(R.id.button_explore)
        btn_explore.setOnClickListener {startActivity(Intent(this, ExploreActivity::class.java)) }

        val listView = findViewById<ListView>(R.id.articleListView)
        val articleList = listOf(
            Article(R.drawable.img_sample, "Article title 1", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample2, "Article title 2", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample3, "Article title 3", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample, "Article title 4", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample2, "Article title 5", getString(R.string.lorem_ipsum)),
            Article(R.drawable.img_sample3, "Article title 6", getString(R.string.lorem_ipsum))
        )

        listView.adapter = ArticleCustomListView(
            this,
            articleList,
            onClick = {
                startActivity (
                    Intent(this, ExploreArticleActivity::class.java).apply {
                        putExtra("imageResource", it.imgResId)
                        putExtra("title", it.title)
                        putExtra("articleText", it.articleText)
                        putExtra("caller", "landing")
                    }
                )
            }
        )

    }
}