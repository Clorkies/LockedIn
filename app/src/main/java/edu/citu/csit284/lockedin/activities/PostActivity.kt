package edu.citu.csit284.lockedin.activities

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostActivity : AppCompatActivity() {

    private val firestore = Firebase.firestore
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val users = Firebase.firestore.collection("users")
    private lateinit var btnProfile: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var etComment: EditText
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        postId = intent.getStringExtra("postId") ?: ""
        btnProfile = findViewById(R.id.btnProfile)
        btnBack = findViewById(R.id.button_back)
        btnBack.setOnClickListener { finish() }
        etComment = findViewById(R.id.etComment)

        etComment.setOnEditorActionListener { _, actionId, event ->
            if (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val commentText = etComment.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    saveComment(postId, commentText)
                } else {
                    toast("Please enter a comment")
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        if (postId.isNotEmpty()) {
            getPostData(postId)
        } else {
            toast("Post ID is null")
            finish()
        }
    }

    private fun getPostData(postId: String) {
        coroutineScope.launch {
            try {
                val postDocument = findPostAcrossGames(postId)

                if (postDocument != null) {
                    val authorUsername = postDocument.getString("authorUsername")
                    val title = postDocument.getString("title")
                    val description = postDocument.getString("description")
                    val timestamp = postDocument.getTimestamp("timestamp")?.toDate()?.time ?: 0
                    val imageUrl = postDocument.getString("imageUrl")

                    withContext(Dispatchers.Main) {
                        displayPostData(authorUsername, title, description, timestamp, imageUrl)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Error: ${e.message}")
                    finish()
                }
            }
        }
    }
    private suspend fun findPostAcrossGames(postId: String): DocumentSnapshot? {
        val games = listOf("valorant", "lol", "csgo", "dota2", "marvel-rivals", "overwatch")
        for (game in games) {
            val postRef = firestore.collection("forums").document(game).collection("posts").document(postId)
            val document = postRef.get().await()
            if (document.exists()) {
                return document
            }
        }
        return null
    }
    private fun displayPostData(
        authorUsername: String?,
        title: String?,
        description: String?,
        timestamp: Long,
        imageUrl: String?
    ) {

        val tvAuthor = findViewById<TextView>(R.id.author)
        val tvTitle = findViewById<TextView>(R.id.title)
        val tvBody = findViewById<TextView>(R.id.body)
        val tvDate = findViewById<TextView>(R.id.date)
        val imageView = findViewById<ImageView>(R.id.image)

        tvAuthor.text = authorUsername
        tvTitle.text = title
        tvBody.text = description
        tvDate.text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(timestamp))

        setupPfp(tvAuthor.text.toString())
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }
    }
    private fun setupPfp(userInfo: String) {
        var pfp: Int
        users.whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    pfp = document.getLong("pfpID")?.toInt() ?: 2
                    when (pfp) {
                        1 -> {
                            btnProfile.setImageResource(R.drawable.red_pfp)
                        }

                        2 -> {
                            btnProfile.setImageResource(R.drawable.default_pfp)
                        }

                        3 -> {
                            btnProfile.setImageResource(R.drawable.green_pfp)
                        }

                        4 -> {
                            btnProfile.setImageResource(R.drawable.blue_pfp)
                        }
                    }
                }
            }
    }
    private fun saveComment(postId: String, commentText: String) {
        coroutineScope.launch {
            try {
                val gameName = getGameName(postId)

                if (gameName != null) {
                    val sharedPrefs = getSharedPreferences("User", Context.MODE_PRIVATE)
                    val authorUsername = sharedPrefs.getString("username", "Anonymous") ?: "Anonymous"
                    val timestamp = FieldValue.serverTimestamp()

                    val commentData = hashMapOf(
                        "authorUsername" to authorUsername,
                        "text" to commentText,
                        "timestamp" to timestamp
                    )

                    firestore.collection("forums").document(gameName)
                        .collection("posts").document(postId)
                        .collection("comments")
                        .add(commentData)
                        .await()

                    withContext(Dispatchers.Main) {
                        etComment.text.clear()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }
    private suspend fun getGameName(postId: String): String? {
        val games = listOf("valorant", "lol", "csgo", "dota2", "marvel-rivals", "overwatch")
        for (game in games) {
            val postRef = firestore.collection("forums").document(game).collection("posts").document(postId)
            val document = postRef.get().await()
            if (document.exists()) {
                return game
            }
        }
        return null
    }
}
