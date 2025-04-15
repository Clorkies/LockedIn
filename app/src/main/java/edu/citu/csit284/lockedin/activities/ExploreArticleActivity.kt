package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.updateArticles
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class ExploreArticleActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_article)

        val backBtn = findViewById<ImageButton>(R.id.button_back)
        backBtn.setOnClickListener { finish() }

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

        val bookmark = findViewById<CheckBox>(R.id.bookmarkCheckbox)

        setInitialBookmarkState(bookmark, title, articleUrl, imageUrl, articleText, publishedDate)

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
                .setDuration(1850)
                .setInterpolator(AccelerateDecelerateInterpolator())
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

    private fun setInitialBookmarkState(
        checkbox: CheckBox,
        title: String,
        articleUrl: String?,
        imageUrl: String?,
        articleText: String,
        publishedDate: String
    ) {
        checkbox.setOnCheckedChangeListener(null)

        checkbox.isChecked = false

        val sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username == null || articleUrl == null) {
            setCheckboxListener(checkbox, title, articleUrl, imageUrl, articleText, publishedDate)
            return
        }

        users
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { userDocs ->
                if (userDocs.documents.isNotEmpty()) {
                    val userDoc = userDocs.documents[0]
                    val bookmarks = userDoc.get("bookmarks") as? List<Map<String, Any>> ?: listOf()

                    val isBookmarked = bookmarks.any {
                        it["title"] == title && it["url"] == articleUrl
                    }

                    checkbox.isChecked = isBookmarked
                    Log.d("Bookmarks", "Article is bookmarked: $isBookmarked")
                }

                setCheckboxListener(checkbox, title, articleUrl, imageUrl, articleText, publishedDate)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking bookmark status", e)
                setCheckboxListener(checkbox, title, articleUrl, imageUrl, articleText, publishedDate)
            }
    }

    private fun setCheckboxListener(
        checkbox: CheckBox,
        title: String,
        articleUrl: String?,
        imageUrl: String?,
        articleText: String,
        publishedDate: String
    ) {
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE)
            val username = sharedPref.getString("username", null)

            if (username == null || articleUrl == null) {
                Toast.makeText(this, "Please log in to bookmark articles", Toast.LENGTH_SHORT).show()
                checkbox.isChecked = !isChecked
                return@setOnCheckedChangeListener
            }

            val bookmarkData = mapOf(
                "title" to title,
                "url" to articleUrl,
                "imageUrl" to (imageUrl ?: ""),
                "description" to articleText,
                "date" to publishedDate
            )

            users
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener { userDocs ->
                    if (userDocs.documents.isEmpty()) {
                        Toast.makeText(this, "User account not found", Toast.LENGTH_SHORT).show()
                        checkbox.isChecked = !isChecked
                        return@addOnSuccessListener
                    }

                    val docRef = userDocs.documents[0].reference

                    if (isChecked) {
                        docRef.update("bookmarks", FieldValue.arrayUnion(bookmarkData))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Added to bookmarks", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error adding bookmark", e)
                                checkbox.isChecked = false
                                Toast.makeText(this, "Failed to bookmark article", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        docRef.get().addOnSuccessListener { docSnapshot ->
                            val existingBookmarks = docSnapshot.get("bookmarks") as? List<Map<String, Any>> ?: listOf()
                            val matchingBookmark = existingBookmarks.find {
                                it["title"] == title && it["url"] == articleUrl
                            }

                            if (matchingBookmark != null) {
                                docRef.update("bookmarks", FieldValue.arrayRemove(matchingBookmark))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error removing bookmark", e)
                                        checkbox.isChecked = true
                                        Toast.makeText(this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                docRef.update("bookmarks", FieldValue.arrayRemove(bookmarkData))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error removing bookmark", e)
                                        checkbox.isChecked = true
                                        Toast.makeText(this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error managing bookmarks", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error fetching user document", e)
                    checkbox.isChecked = !isChecked
                }
        }
    }
}
