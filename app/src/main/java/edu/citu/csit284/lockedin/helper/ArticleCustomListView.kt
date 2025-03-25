package edu.citu.csit284.lockedin.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Article

class ArticleCustomListView(
    private val context: Context,
    private val articleList: List<Article>,
    private val onClick: (Article) -> Unit
) : BaseAdapter() {
    override fun getCount(): Int = articleList.size
    override fun getItem(pos: Int): Any = articleList[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.activity_article_custom_listview,
            parent,
            false
        )

        val img = view.findViewById<ImageView>(R.id.articleImage)
        val title = view.findViewById<TextView>(R.id.articleTitle)
        val text = view.findViewById<TextView>(R.id.articleText)

        val article = articleList[pos]

        img.setImageResource(article.imgResId)
        title.setText(article.title)
        text.setText(article.articleText)

        view.setOnClickListener {
            onClick(article)
        }

        return view
    }

}