package edu.citu.csit284.lockedin.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Article

class ArticleAdapter(
    private val context: Context,
    private val articles: List<Article>
) : ArrayAdapter<Article>(context, 0, articles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.activity_article_custom_listview, parent, false)
        }

        val article = articles[position]

        val imageView = itemView!!.findViewById<ImageView>(R.id.articleImage)
        val titleView = itemView.findViewById<TextView>(R.id.articleTitle)
        val descView = itemView.findViewById<TextView>(R.id.articleText)

        imageView.setImageResource(article.imgResId)
        titleView.text = article.title
        descView.text = article.articleText

        return itemView
    }
}
