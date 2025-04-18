package edu.citu.csit284.lockedin.fragments

import PostAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.activities.CreatePostActivity
import com.google.firebase.firestore.ktx.firestore
import edu.citu.csit284.lockedin.data.Post
import edu.citu.csit284.lockedin.helper.BottomSpace
import edu.citu.csit284.lockedin.util.setupHeaderScrollBehavior

class ForumFragment : Fragment(), PostAdapter.OnItemClickListener {

    private var caller: String? = null
    private val users = Firebase.firestore.collection("users")
    private val forums = Firebase.firestore.collection("forums")
    private lateinit var headerContainer: LinearLayout
    private lateinit var btnGame1: LinearLayout
    private lateinit var btnGame1Text: TextView
    private lateinit var btnGame2: LinearLayout
    private lateinit var btnGame2Text: TextView
    private lateinit var btnGame3: LinearLayout
    private lateinit var btnGame3Text: TextView
    private var prefNames: List<String> = emptyList()
    private lateinit var sharedPref: SharedPreferences
    private var userInfo: String? = null
    private var currentCategory = "game1"
    private var previousCategory = "game1"
    private lateinit var btnProfile : ImageButton
    private val gamesMap = mapOf(
        1 to "valorant",
        2 to "lol",
        3 to "csgo",
        4 to "dota2",
        5 to "marvel-rivals",
        6 to "overwatch"
    )
    private lateinit var fabCreate: MaterialButton
    private lateinit var rvView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private var voter : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
        sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        userInfo = sharedPref.getString("username", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnProfile = view.findViewById(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "forum" -> {}
                "explore" -> findNavController().navigate(R.id.exploreFragment)
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        headerContainer = view.findViewById(R.id.headerContainer)
        btnGame1 = view.findViewById(R.id.game1Btn)
        btnGame1Text = view.findViewById(R.id.game1)
        btnGame2 = view.findViewById(R.id.game2Btn)
        btnGame2Text = view.findViewById(R.id.game2)
        btnGame3 = view.findViewById(R.id.game3Btn)
        btnGame3Text = view.findViewById(R.id.game3)
        fabCreate = view.findViewById(R.id.fabCreate)
        fabCreate.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            val currentGame = getCurrentGame()
            intent.putExtra("currentGame", currentGame)
            startActivity(intent)
        }

        rvView = view.findViewById(R.id.rvView)
        rvView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = userInfo?.let { PostAdapter(postList, this, requireContext(), it) }!!
        rvView.adapter = postAdapter

        // Para di matago behind the navbar ang last item sa scroll/listview
        val bottomSpace = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            110f,
            resources.displayMetrics
        ).toInt()
        rvView.addItemDecoration(BottomSpace(bottomSpace))
        ////

        setupHeaderScrollBehavior(headerContainer, rvView)

        setupPfp()
        loadFavoriteGames()
        loadPostsForCategory(currentCategory)
    }

    private fun loadFavoriteGames() {
        userInfo?.let {
            users.whereEqualTo("username", it)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val rawList = document.get("favGames") as? List<Long>
                        prefNames = rawList
                            ?.map { it.toInt() }
                            ?.mapNotNull { gamesMap[it] }
                            ?: emptyList()

                        setupFavoriteGamesButtons()
                    }
                }
        }
    }

    private fun setupFavoriteGamesButtons() {
        if (prefNames.size >= 1) {
            setupGameButton(btnGame1, btnGame1Text, prefNames[0], 1)
            btnGame1.setOnClickListener {
                if (currentCategory != "game1") {
                    switchCategory("game1", prefNames[0])
                }
            }
        } else {
            btnGame1.visibility = View.GONE
        }

        if (prefNames.size >= 2) {
            setupGameButton(btnGame2, btnGame2Text, prefNames[1], 2)
            btnGame2.setOnClickListener {
                if (currentCategory != "game2") {
                    switchCategory("game2", prefNames[1])
                }
            }
        } else {
            btnGame2.visibility = View.GONE
        }

        if (prefNames.size >= 3) {
            setupGameButton(btnGame3, btnGame3Text, prefNames[2], 3)
            btnGame3.setOnClickListener {
                if (currentCategory != "game3") {
                    switchCategory("game3", prefNames[2])
                }
            }
        } else {
            btnGame3.visibility = View.GONE
        }

        updateButtonStyles(currentCategory)
    }

    private fun setupGameButton(button: LinearLayout, textView: TextView, gameName: String, buttonIndex: Int) {
        button.setPadding(20)

        textView.tag = gameName
        textView.text = gameName.replaceFirstChar { it.uppercase() }
        val logoName = when (gameName.lowercase()) {
            "valorant" -> "valorant"
            "lol" -> "league"
            "csgo" -> "csgo"
            "dota2" -> "dota"
            "marvel-rivals" -> "rivals"
            "overwatch" -> "overwatch"
            else -> ""
        }
        var logoView = button.findViewWithTag<ImageView>("gameLogoImage$buttonIndex")
        if (logoView == null) {
            logoView = ImageView(requireContext())
            logoView.tag = "gameLogoImage$buttonIndex"
            logoView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            button.addView(logoView, 0)
        }

        val logoResId = resources.getIdentifier(
            "logo_$logoName",
            "drawable",
            requireContext().packageName
        )

        if (logoResId != 0) {
            logoView.setImageResource(logoResId)
        }

        button.tag = "game$buttonIndex"
    }

    private fun switchCategory(newCategory: String, newGame: String) {
        resetButtonToInactive(previousCategory)
        currentCategory = newCategory
        updateButtonStyles(newCategory)
        loadPostsForCategory(newCategory)
    }

    private fun updateButtonStyles(activeCategory: String) {
        resetButtonToInactive(previousCategory)
        when (activeCategory) {
            "game1" -> setButtonActive(btnGame1, btnGame1.findViewWithTag("gameLogoImage1"), btnGame1Text, btnGame1Text)
            "game2" -> setButtonActive(btnGame2, btnGame2.findViewWithTag("gameLogoImage2"), btnGame2Text, btnGame2Text)
            "game3" -> setButtonActive(btnGame3, btnGame3.findViewWithTag("gameLogoImage3"), btnGame3Text, btnGame3Text)
        }
        previousCategory = activeCategory
    }

    private fun resetButtonToInactive(category: String) {
        val inactiveBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_bg)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.white)

        when (category) {
            "game1" -> {
                btnGame1.background = inactiveBackground
                btnGame1Text.setTextColor(inactiveTextColor)
                btnGame1Text.visibility = View.GONE
                btnGame1.findViewWithTag<ImageView>("gameLogoImage1")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame1, 250)
            }
            "game2" -> {
                btnGame2.background = inactiveBackground
                btnGame2Text.setTextColor(inactiveTextColor)
                btnGame2Text.visibility = View.GONE
                btnGame2.findViewWithTag<ImageView>("gameLogoImage2")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame2, 250)
            }
            "game3" -> {
                btnGame3.background = inactiveBackground
                btnGame3Text.setTextColor(inactiveTextColor)
                btnGame3Text.visibility = View.GONE
                btnGame3.findViewWithTag<ImageView>("gameLogoImage3")?.visibility = View.VISIBLE
                animateButtonWidth(btnGame3, 250)
            }
        }
    }

    private fun setButtonActive(button: View, iconView: View?, textView: View, textViewForColor: TextView? = null) {
        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_rounded_titles_bg_selected)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.bg)

        animateButtonWidth(button, 350)

        button.postDelayed({
            button.background = activeBackground
            iconView?.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textViewForColor?.setTextColor(activeTextColor)
        }, 300)
    }

    private fun animateButtonWidth(button: View, targetWidth: Int) {
        val animator = ValueAnimator.ofInt(button.width, targetWidth)
        animator.addUpdateListener { valueAnimator ->
            val params = button.layoutParams
            params.width = valueAnimator.animatedValue as Int
            button.layoutParams = params
        }
        animator.duration = 350
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun getCurrentGame(): String? {
        return when (currentCategory) {
            "game1" -> prefNames.getOrNull(0)
            "game2" -> prefNames.getOrNull(1)
            "game3" -> prefNames.getOrNull(2)
            else -> null
        }
    }

    private fun loadPostsForCategory(categoryName: String) {
        val gameName = when (categoryName) {
            "game1" -> prefNames.getOrNull(0) ?: "valorant"
            "game2" -> prefNames.getOrNull(1) ?: "lol"
            "game3" -> prefNames.getOrNull(2) ?: "csgo"
            else -> "valorant"
        }
        forums.firestore.collection("forums/$gameName/posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val newPosts = querySnapshot.documents.map { document ->
                    mapDocumentToPost(document)
                }
                postList.clear()
                postList.addAll(newPosts)
                postAdapter.notifyDataSetChanged()
            }
    }

    private fun mapDocumentToPost(document: DocumentSnapshot): Post {
        val timestamp = document.get("timestamp")
        val timestampLong = if (timestamp is Timestamp) {
            timestamp.toDate().time
        } else {
            0
        }
        val upvotedBy = document.get("upvotedBy") as? List<String> ?: emptyList()
        val downvotedBy = document.get("downvotedBy") as? List<String> ?: emptyList()
        if(upvotedBy.contains(userInfo) || downvotedBy.contains(userInfo)){
            voter = true
        }
        return Post(
            id = document.id,
            authorUsername = document.getString("authorUsername"),
            title = document.getString("title"),
            description = document.getString("description"),
            imageUrl = document.getString("imageUrl"),
            upvotes = document.getLong("upvotes")?.toInt() ?: 0,
            downvotes = document.getLong("downvotes")?.toInt() ?: 0,
            timestamp = timestampLong,
            upvotedBy = upvotedBy.toMutableList(),
            downvotedBy = downvotedBy.toMutableList()
        )
    }

    override fun onUpvoteClick(
        position: Int,
        currentUpvotes: Int,
        currentDownvotes: Int,
        alreadyUpvoted: Boolean,
        alreadyDownvoted: Boolean
    ) {
        val post = postList[position]
        val gameName = getCurrentGame() ?: "valorant"
        val postRef = forums.firestore.collection("forums/$gameName/posts")
            .document(post.id)

        if (alreadyUpvoted) {
            postRef.update(
                "upvotes", currentUpvotes - 1,
                "upvotedBy", FieldValue.arrayRemove(userInfo)
            ).addOnSuccessListener {
                val updatedUpvotedBy = post.upvotedBy.filter { it != userInfo }.toMutableList()
                postList[position] =
                    post.copy(upvotes = currentUpvotes - 1, upvotedBy = updatedUpvotedBy)
                postAdapter.updateUpvoteCount(position, currentUpvotes - 1)
                postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
            }
        } else if (alreadyDownvoted) {
            postRef.update(
                "upvotes", currentUpvotes + 1,
                "downvotes", currentDownvotes - 1,
                "upvotedBy", FieldValue.arrayUnion(userInfo),
                "downvotedBy", FieldValue.arrayRemove(userInfo)
            ).addOnSuccessListener {
                val updatedUpvotedBy = post.upvotedBy.toMutableList()
                userInfo?.let { it1 -> updatedUpvotedBy.add(it1) }

                val updatedDownvotedBy = post.downvotedBy.filter { it != userInfo }.toMutableList()

                postList[position] = post.copy(
                    upvotes = currentUpvotes + 1,
                    downvotes = currentDownvotes - 1,
                    upvotedBy = updatedUpvotedBy,
                    downvotedBy = updatedDownvotedBy
                )
                postAdapter.updateUpvoteCount(position, currentUpvotes + 1)
                postAdapter.updateDownvoteCount(position, currentDownvotes - 1)
                postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
            }
        } else {
            postRef.update(
                "upvotes", currentUpvotes + 1,
                "upvotedBy", FieldValue.arrayUnion(userInfo)
            ).addOnSuccessListener {
                val updatedUpvotedBy = post.upvotedBy.toMutableList()
                userInfo?.let { it1 -> updatedUpvotedBy.add(it1) }

                postList[position] =
                    post.copy(upvotes = currentUpvotes + 1, upvotedBy = updatedUpvotedBy)
                postAdapter.updateUpvoteCount(position, currentUpvotes + 1)
                postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
            }
        }
    }
    override fun onDownvoteClick(
        position: Int,
        currentUpvotes: Int,
        currentDownvotes: Int,
        alreadyUpvoted: Boolean,
        alreadyDownvoted: Boolean
    ) {
        val post = postList[position]
        val gameName = getCurrentGame() ?: "valorant"
        val postRef = forums.firestore.collection("forums/$gameName/posts")
            .document(post.id)

        if (alreadyDownvoted) {
            postRef.update(
                "downvotes", currentDownvotes - 1,
                "downvotedBy", FieldValue.arrayRemove(userInfo)
            ).addOnSuccessListener {
                val updatedDownvotedBy = post.downvotedBy.filter { it != userInfo }.toMutableList()
                postList[position] = post.copy(downvotes = currentDownvotes - 1, downvotedBy = updatedDownvotedBy)
                postAdapter.updateDownvoteCount(position, currentDownvotes - 1)
                postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
            }
        } else if (alreadyUpvoted) {
            postRef.update(
                "upvotes", currentUpvotes - 1,
                "downvotes", currentDownvotes + 1,
                "upvotedBy", FieldValue.arrayRemove(userInfo),
                "downvotedBy", FieldValue.arrayUnion(userInfo)
            ).addOnSuccessListener {
                val updatedUpvotedBy = post.upvotedBy.filter { it != userInfo }.toMutableList()
                val updatedDownvotedBy = post.downvotedBy.toMutableList()
                userInfo?.let { it1 -> updatedDownvotedBy.add(it1) }

                postList[position] = post.copy(
                    upvotes = currentUpvotes - 1,
                    downvotes = currentDownvotes + 1,
                    upvotedBy = updatedUpvotedBy,
                    downvotedBy = updatedDownvotedBy
                )
                postAdapter.updateUpvoteCount(position, currentUpvotes - 1)
                postAdapter.updateDownvoteCount(position, currentDownvotes + 1)
                postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
            }
        } else {
            postRef.update(
                "downvotes", currentDownvotes + 1,
                "downvotedBy", FieldValue.arrayUnion(userInfo)
            ).addOnSuccessListener {
                val updatedDownvotedBy = post.downvotedBy.toMutableList()
                userInfo?.let { it1 -> updatedDownvotedBy.add(it1) }

                postList[position] = post.copy(downvotes = currentDownvotes + 1, downvotedBy = updatedDownvotedBy)
                postAdapter.updateDownvoteCount(position, currentDownvotes + 1)
                postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
            }
        }
    }

    private fun setupPfp() {
        var pfp: Int
        users
            .whereEqualTo("username", userInfo)
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
    override fun onItemClick(position: Int) {
    }
}

