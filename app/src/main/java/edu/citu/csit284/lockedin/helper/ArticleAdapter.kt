package edu.citu.csit284.lockedin.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Article
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ArticleAdapter(
    private val context: Context,
    private val articles: List<Article>
) : ArrayAdapter<Article>(context, 0, articles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.activity_article_custom_listview, parent, false
        )

        val article = articles[position]

        val imageView = itemView.findViewById<ImageView>(R.id.articleImage)
        val titleView = itemView.findViewById<TextView>(R.id.articleTitle)
        val descView = itemView.findViewById<TextView>(R.id.articleText)
        val dateView = itemView.findViewById<TextView>(R.id.articleDate)

        if (!article.urlToImage.isNullOrEmpty()) {
            Picasso.get()
                .load(article.urlToImage)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.img_placeholder)
        }

        titleView.text = article.title ?: "No title"
        descView.text = article.description ?: "No description"

        article.publishedAt?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                date?.let { parsedDate ->
                    dateView.text = outputFormat.format(parsedDate)
                    dateView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                dateView.text = it
                dateView.visibility = View.VISIBLE
            }
        } ?: run {
            dateView.visibility = View.GONE
        }

        return itemView
    }

    override fun getCount() : Int = articles.size
}

