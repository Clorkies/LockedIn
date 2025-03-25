package edu.citu.csit284.lockedin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import edu.citu.csit284.lockedin.data.Article
import edu.citu.csit284.lockedin.helper.ArticleCustomListView

class ExploreActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener {startActivity(Intent(this, LandingActivity::class.java)) }

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
                    }
                )
            }
        )
    }
}