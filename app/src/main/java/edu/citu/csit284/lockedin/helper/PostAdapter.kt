import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R // Import your R file
import edu.citu.csit284.lockedin.data.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(private val listOfPosts: List<Post>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<PostAdapter.ItemViewHolder>() {  // Changed ViewHolder name
    private val users = Firebase.firestore.collection("users")
    interface OnItemClickListener {
        fun onUpvoteClick(position: Int)
        fun onDownvoteClick(position: Int)
        fun onItemClick(position: Int)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPostTitle: TextView = itemView.findViewById(R.id.postTitle)
        val tvPostBody: TextView = itemView.findViewById(R.id.postBody)
        val imgPostImage: ImageView = itemView.findViewById(R.id.postImage)
        val btnUpvote: ImageView = itemView.findViewById(R.id.upvoteButton)
        val btnDownvote: ImageView = itemView.findViewById(R.id.downvoteButton)
        val tvUpvoteCount: TextView = itemView.findViewById(R.id.upvoteCount)
        val tvDownvoteCount: TextView = itemView.findViewById(R.id.downvoteCount)
        val imgProfilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val tvUserName: TextView = itemView.findViewById(R.id.userName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.timestamp)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    (it.context as? OnItemClickListener)?.onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val post = listOfPosts[position]

        holder.tvPostTitle.text = post.title
        holder.tvPostBody.text = post.description
        holder.tvUserName.text = post.authorUsername
        post.authorUsername?.let { setProfilePicture(it, holder.imgProfilePicture) }

        val formattedTimestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(post.timestamp))
        holder.tvTimestamp.text = formattedTimestamp

        if (post.imageUrl != null && post.imageUrl.isNotEmpty()) {
            holder.imgPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .apply(RequestOptions().override(600, 400))
                .into(holder.imgPostImage)
        } else {
            holder.imgPostImage.visibility = View.GONE
        }


        holder.tvUpvoteCount.text = post.upvotes.toString()
        holder.tvDownvoteCount.text = post.downvotes.toString()

        holder.btnUpvote.setOnClickListener {
            itemClickListener.onUpvoteClick(position)
        }
        holder.btnDownvote.setOnClickListener {
            itemClickListener.onDownvoteClick(position)
        }
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
        return listOfPosts.size
    }
}

