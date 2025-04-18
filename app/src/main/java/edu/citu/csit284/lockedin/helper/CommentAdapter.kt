package edu.citu.csit284.lockedin.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private val users = Firebase.firestore.collection("users")
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profilePicture)
        val userNameTextView: TextView = itemView.findViewById(R.id.userName)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestamp)
        val bodyTextView: TextView = itemView.findViewById(R.id.body)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.userNameTextView.text = comment.authorUsername
        holder.bodyTextView.text = comment.description

        val formattedTimestamp = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(
            Date(comment.timestamp)
        )
        holder.timestampTextView.text = formattedTimestamp

        setProfilePicture(holder.userNameTextView.text.toString(), holder.profilePictureImageView)
    }
    private fun setProfilePicture(username: String, imgProfilePicture: ImageView) {
        users.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val pfpId = document.getLong("pfpID")?.toInt() ?: 2

                    val drawableResId = when (pfpId) {
                        1 -> R.drawable.red_pfp
                        2 -> R.drawable.default_pfp
                        3 -> R.drawable.green_pfp
                        4 -> R.drawable.blue_pfp
                        else -> R.drawable.default_pfp
                    }
                    imgProfilePicture.setImageResource(drawableResId)
                } else {
                    imgProfilePicture.setImageResource(R.drawable.default_pfp)
                }
            }
    }
    override fun getItemCount(): Int {
        return comments.size
    }
}
