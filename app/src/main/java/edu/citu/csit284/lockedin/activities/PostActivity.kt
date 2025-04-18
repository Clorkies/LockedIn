package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Comment
import edu.citu.csit284.lockedin.helper.CommentAdapter
import edu.citu.csit284.lockedin.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth

class PostActivity : Activity() {

    private val firestore = Firebase.firestore
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val users = Firebase.firestore.collection("users")
    private lateinit var btnProfile: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var etComment: EditText
    private lateinit var postId: String
    private lateinit var rvComments: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private var userUid: String? = null
    private var authorUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        postId = intent.getStringExtra("postId") ?: ""
        userUid = FirebaseAuth.getInstance().currentUser?.uid
        btnProfile = findViewById(R.id.btnProfile)
        btnBack = findViewById(R.id.button_back)
        btnBack.setOnClickListener { finish() }
        etComment = findViewById(R.id.etComment)
        rvComments = findViewById(R.id.comments)
        rvComments.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(commentList)
        rvComments.adapter = commentAdapter

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
            loadComments()
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
                    authorUid = postDocument.getString("authorUid")
                    val title = postDocument.getString("title")
                    val description = postDocument.getString("description")
                    val timestamp = postDocument.getTimestamp("timestamp")?.toDate()?.time ?: 0
                    val imageUrl = postDocument.getString("imageUrl")

                    var authorName: String? = null
                    if (authorUid != null) {
                        authorName = getUsernameByUid(authorUid!!)
                    }
                    val localAuthorName = authorName
                    withContext(Dispatchers.Main) {
                        displayPostData(localAuthorName, title, description, timestamp, imageUrl)
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
        val games = listOf("valorant", "lol", "csgo", "dota2", "mlbb", "overwatch")
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

        if (authorUsername != null) {
            coroutineScope.launch{
                val pfpId = getPfpIdByUid(authorUid)
                withContext(Dispatchers.Main){
                    setupPfp(pfpId)
                }
            }
        } else {
            btnProfile.setImageResource(R.drawable.default_pfp)
        }

        if (imageUrl != null && imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }
    }

    private fun setupPfp(pfpId: Int) {
        var pfp: Int = pfpId
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

    private fun saveComment(postId: String, commentText: String) {
        coroutineScope.launch {
            try {
                val gameName = getGameName(postId)

                if (gameName != null) {
                    val authorUid = userUid ?: "Anonymous"
                    val commentData = hashMapOf(
                        "authorUid" to authorUid,
                        "text" to commentText,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    val newCommentRef = firestore.collection("forums").document(gameName)
                        .collection("posts").document(postId)
                        .collection("comments")
                        .document()
                    newCommentRef.set(commentData).await()

                    val savedCommentSnapshot = newCommentRef.get().await()
                    val savedTimestamp = savedCommentSnapshot.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis()

                    val newComment = Comment(
                        id = newCommentRef.id,
                        authorUid = authorUid,
                        description = commentText,
                        timestamp = savedTimestamp
                    )

                    withContext(Dispatchers.Main) {
                        etComment.text.clear()
                        commentList.add(0, newComment)
                        commentAdapter.notifyItemInserted(0)
                        loadComments()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Error saving comment: ${e.message}")
                }
            }
        }
    }

    private suspend fun getGameName(postId: String): String? {
        val games = listOf("valorant", "lol", "csgo", "dota2", "mlbb", "overwatch")
        for (game in games) {
            val postRef = firestore.collection("forums").document(game).collection("posts").document(postId)
            val document = postRef.get().await()
            if (document.exists()) {
                return game
            }
        }
        return null
    }

    private fun loadComments() {
        coroutineScope.launch {
            try {
                val gameName = getGameName(postId) ?: ""
                if (gameName.isNotEmpty()) {
                    val commentsRef = firestore.collection("forums").document(gameName)
                        .collection("posts").document(postId)
                        .collection("comments")
                    val querySnapshot = commentsRef.orderBy("timestamp", Query.Direction.DESCENDING).get().await()

                    val fetchedComments = querySnapshot.documents.map { document ->
                        val timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0
                        val authorUid = document.getString("authorUid")
                        val authorName = if (authorUid != null) getUsernameByUid(authorUid) else "Anonymous"
                        Comment(
                            id = document.id,
                            authorUid = authorUid,
                            description = document.getString("text"),
                            timestamp = timestamp
                        )
                    }
                    withContext(Dispatchers.Main) {
                        commentList.clear()
                        commentList.addAll(fetchedComments)
                        commentAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Error loading comments: ${e.message}")
                }
            }
        }
    }

    private suspend fun getUsernameByUid(uid: String): String {
        return try {
            val userSnapshot = users.whereEqualTo("uid", uid).get().await()
            if (!userSnapshot.isEmpty) {
                userSnapshot.documents[0].getString("username") ?: "Anonymous"
            } else {
                "Anonymous"
            }
        } catch (e: Exception) {
            Log.e("PostActivity", "Error fetching username: $e")
            "Anonymous"
        }
    }

    private suspend fun getPfpIdByUid(uid: String?): Int {
        return try {
            if(uid == null){
                return 2
            }
            val userSnapshot = users.whereEqualTo("uid", uid).get().await()
            if (!userSnapshot.isEmpty) {
                userSnapshot.documents[0].getLong("pfpID")?.toInt() ?: 2
            } else {
                2
            }
        } catch (e: Exception) {
            Log.e("PostActivity", "Error fetching pfpID: $e")
            2
        }
    }
}

