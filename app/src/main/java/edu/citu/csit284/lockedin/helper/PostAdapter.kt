import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private val listOfPosts: MutableList<Post>,
    private val itemClickListener: OnItemClickListener,
    private val context: Context,
    private var currentUserName: String = (FirebaseAuth.getInstance().currentUser?.uid ?: "").trim()
) :
    RecyclerView.Adapter<PostAdapter.ItemViewHolder>() {
    private val users = Firebase.firestore.collection("users")
    private var currentUserUid: String = ""

    init {
        if (currentUserName.isNotEmpty()) {
            users.whereEqualTo("username", currentUserName)
                .get()
                .addOnSuccessListener { userDocuments ->
                    if (!userDocuments.isEmpty) {
                        val userDocument = userDocuments.documents[0]
                        currentUserUid = userDocument.getString("uid") ?: ""
                        Log.d("PostAdapter", "Got UID from Firestore: $currentUserUid")
                        notifyDataSetChanged()
                    } else {
                        Log.e("PostAdapter", "No user found with username: $currentUserName")
                        currentUserUid = ""
                        notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PostAdapter", "Error getting UID: $exception")
                    currentUserUid = ""
                    notifyDataSetChanged()
                }
        } else {
            currentUserUid = ""
            notifyDataSetChanged()
        }
    }


    interface OnItemClickListener {
        fun onUpvoteClick(
            position: Int,
            currentUpvotes: Int,
            currentDownvotes: Int,
            alreadyUpvoted: Boolean,
            alreadyDownvoted: Boolean
        )

        fun onDownvoteClick(
            position: Int,
            currentUpvotes: Int,
            currentDownvotes: Int,
            alreadyUpvoted: Boolean,
            alreadyDownvoted: Boolean
        )

        fun onItemClick(position: Int)
    }

    class ItemViewHolder(itemView: View, val clickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
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
                    clickListener.onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ItemViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val post = listOfPosts[position]
        holder.tvPostTitle.text = post.title
        holder.tvPostBody.text = post.description
        users.whereEqualTo("uid", post.authorUid)
            .get()
            .addOnSuccessListener { userDocuments ->
                if (!userDocuments.isEmpty) {
                    val userDocument = userDocuments.documents[0]
                    val authorUsername = userDocument.getString("username")
                    holder.tvUserName.text = authorUsername
                    setProfilePicture(
                        userDocument.getLong("pfpID")?.toInt() ?: 2,
                        holder.imgProfilePicture
                    )
                } else {
                    holder.tvUserName.text = "Unknown User"
                    holder.imgProfilePicture.setImageResource(R.drawable.default_pfp)
                }
            }
        val formattedTimestamp =
            SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(post.timestamp))
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
        Log.d("Current User UID", currentUserUid)
        if (post.upvotedBy.contains(currentUserUid)) {
            holder.tvUpvoteCount.setTextColor(context.getColor(R.color.up))
            holder.btnUpvote.setImageResource(R.drawable.upvote_icon_active)
        } else {
            holder.tvUpvoteCount.setTextColor(context.getColor(R.color.white))
            holder.btnUpvote.setImageResource(R.drawable.upvote_icon_inactive)
        }

        if (post.downvotedBy.contains(currentUserUid)) {
            holder.tvDownvoteCount.setTextColor(context.getColor(R.color.down))
            holder.btnDownvote.setImageResource(R.drawable.downvote_icon_active)
        } else {
            holder.tvDownvoteCount.setTextColor(context.getColor(R.color.white))
            holder.btnDownvote.setImageResource(R.drawable.downvote_icon_inactive)
        }

        holder.btnUpvote.setOnClickListener {
            val alreadyUpvoted = post.upvotedBy.contains(currentUserUid)
            val alreadyDownvoted = post.downvotedBy.contains(currentUserUid)
            itemClickListener.onUpvoteClick(
                position,
                post.upvotes,
                post.downvotes,
                alreadyUpvoted,
                alreadyDownvoted
            )
        }
        holder.btnDownvote.setOnClickListener {
            val alreadyUpvoted = post.upvotedBy.contains(currentUserUid)
            val alreadyDownvoted = post.downvotedBy.contains(currentUserUid)
            itemClickListener.onDownvoteClick(
                position,
                post.upvotes,
                post.downvotes,
                alreadyUpvoted,
                alreadyDownvoted
            )
        }
    }

    private fun setProfilePicture(pfpId: Int, imgProfilePicture: ImageView) {
        val drawableResId = when (pfpId) {
            1 -> R.drawable.red_pfp
            2 -> R.drawable.default_pfp
            3 -> R.drawable.green_pfp
            4 -> R.drawable.blue_pfp
            else -> R.drawable.default_pfp
        }
        imgProfilePicture.setImageResource(drawableResId)

    }

    override fun getItemCount(): Int {
        return listOfPosts.size
    }

    fun updateUpvoteCount(position: Int, newCount: Int) {
        if (position < listOfPosts.size) {
            listOfPosts[position].upvotes = newCount
            notifyItemChanged(position)
        }
    }

    fun updateDownvoteCount(position: Int, newCount: Int) {
        if (position < listOfPosts.size) {
            listOfPosts[position].downvotes = newCount
            notifyItemChanged(position)
        }
    }

    fun updateUpvotedBy(position: Int, users: MutableList<String>) {
        if (position < listOfPosts.size) {
            listOfPosts[position].upvotedBy = users
            notifyItemChanged(position)
        }
    }

    fun updateDownvotedBy(position: Int, users: MutableList<String>) {
        if (position < listOfPosts.size) {
            listOfPosts[position].downvotedBy = users
            notifyItemChanged(position)
        }
    }

}
