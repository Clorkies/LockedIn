package edu.citu.csit284.lockedin.fragments

import PostAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.navigation.NavOptions
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
import edu.citu.csit284.lockedin.activities.MainActivity
import edu.citu.csit284.lockedin.activities.PostActivity
import edu.citu.csit284.lockedin.data.Post
import edu.citu.csit284.lockedin.helper.BottomSpace
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    private var userUid: String? = null
    private var currentCategory = "game1"
    private var previousCategory = "game1"
    private lateinit var btnProfile: ImageButton
    private val gamesMap = mapOf(
        1 to "valorant",
        2 to "lol",
        3 to "csgo",
        4 to "dota2",
        5 to "mlbb",
        6 to "overwatch"
    )
    private lateinit var fabCreate: MaterialButton
    private lateinit var rvView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
        sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        userInfo = sharedPref.getString("username", "")
        userUid = FirebaseAuth.getInstance().currentUser?.uid
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
            profileActivityLauncher.launch(Intent(requireContext(), ProfileActivity::class.java))
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

        rvView.addItemDecoration(BottomSpace(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            110f,
            resources.displayMetrics
        ).toInt()))

        setupPfp()
        loadFavoriteGames()
    }

    private val profileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val currentId = (requireActivity() as MainActivity).navController.currentDestination?.id ?: return@registerForActivityResult

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()

        (requireActivity() as MainActivity).apply {
            isNavigatingFromCode = true
            try {
                navController.navigate(currentId, null, navOptions)
            } catch (_: IllegalArgumentException) {}
        }
    }

    private fun loadFavoriteGames() {
        userUid?.let {
            users.whereEqualTo("uid", it)
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
                        loadPostsForCategory(currentCategory)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPostsForCategory(currentCategory)
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
            "mlbb" -> "mlbb"
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

        return Post(
            id = document.id,
            authorUid = document.getString("authorUid"),
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
        val currentUserUid = userUid ?: return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val postSnapshot = postRef.get().await()
                if (postSnapshot.exists()) {
                    val serverUpvotedBy =
                        postSnapshot.get("upvotedBy") as? List<String> ?: emptyList()
                    val serverDownvotedBy =
                        postSnapshot.get("downvotedBy") as? List<String> ?: emptyList()

                    val isAlreadyUpvoted = serverUpvotedBy.contains(currentUserUid)
                    val isAlreadyDownvoted = serverDownvotedBy.contains(currentUserUid)
                    var newUpvotes = postSnapshot.getLong("upvotes")?.toInt() ?: 0
                    var newDownvotes = postSnapshot.getLong("downvotes")?.toInt() ?: 0

                    if (isAlreadyUpvoted) {
                        newUpvotes -= 1
                        postRef.update(
                            "upvotes", newUpvotes,
                            "upvotedBy", FieldValue.arrayRemove(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main) {
                            val updatedUpvotedBy = serverUpvotedBy.filter { it != currentUserUid }.toMutableList()
                            postList[position] =
                                post.copy(upvotes = newUpvotes, upvotedBy = updatedUpvotedBy)
                            postAdapter.updateUpvoteCount(position, newUpvotes)
                            postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                        }

                    } else if (isAlreadyDownvoted) {
                        newUpvotes += 1
                        newDownvotes -= 1
                        postRef.update(
                            "upvotes", newUpvotes,
                            "downvotes", newDownvotes,
                            "upvotedBy", FieldValue.arrayUnion(currentUserUid),
                            "downvotedBy", FieldValue.arrayRemove(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main){
                            val updatedUpvotedBy = (serverUpvotedBy + currentUserUid).toMutableList()
                            val updatedDownvotedBy = serverDownvotedBy.filter { it != currentUserUid }.toMutableList()
                            postList[position] = post.copy(
                                upvotes = newUpvotes,
                                downvotes = newDownvotes,
                                upvotedBy = updatedUpvotedBy,
                                downvotedBy = updatedDownvotedBy
                            )
                            postAdapter.updateUpvoteCount(position, newUpvotes)
                            postAdapter.updateDownvoteCount(position, newDownvotes)
                            postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                            postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
                        }

                    } else {
                        newUpvotes += 1
                        postRef.update(
                            "upvotes", newUpvotes,
                            "upvotedBy", FieldValue.arrayUnion(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main) {
                            val updatedUpvotedBy = (serverUpvotedBy + currentUserUid).toMutableList()
                            postList[position] =
                                post.copy(upvotes = newUpvotes, upvotedBy = updatedUpvotedBy)
                            postAdapter.updateUpvoteCount(position, newUpvotes)
                            postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                }

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
        val currentUserUid = userUid ?: return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val postSnapshot = postRef.get().await()
                if (postSnapshot.exists()) {
                    val serverUpvotedBy =
                        postSnapshot.get("upvotedBy") as? List<String> ?: emptyList()
                    val serverDownvotedBy =
                        postSnapshot.get("downvotedBy") as? List<String> ?: emptyList()

                    val isAlreadyUpvoted = serverUpvotedBy.contains(currentUserUid)
                    val isAlreadyDownvoted = serverDownvotedBy.contains(currentUserUid)

                    var newUpvotes = postSnapshot.getLong("upvotes")?.toInt() ?: 0
                    var newDownvotes = postSnapshot.getLong("downvotes")?.toInt() ?: 0

                    if (isAlreadyDownvoted) {
                        newDownvotes -= 1
                        postRef.update(
                            "downvotes", newDownvotes,
                            "downvotedBy", FieldValue.arrayRemove(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main){
                            val updatedDownvotedBy = serverDownvotedBy.filter { it != currentUserUid }.toMutableList()
                            postList[position] = post.copy(downvotes = newDownvotes, downvotedBy = updatedDownvotedBy)
                            postAdapter.updateDownvoteCount(position, newDownvotes)
                            postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
                        }
                    } else if (isAlreadyUpvoted) {
                        newUpvotes -= 1
                        newDownvotes += 1
                        postRef.update(
                            "upvotes", newUpvotes,
                            "downvotes", newDownvotes,
                            "upvotedBy", FieldValue.arrayRemove(currentUserUid),
                            "downvotedBy", FieldValue.arrayUnion(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main){
                            val updatedUpvotedBy = serverUpvotedBy.filter { it != currentUserUid }.toMutableList()
                            val updatedDownvotedBy = (serverDownvotedBy + currentUserUid).toMutableList()
                            postList[position] = post.copy(
                                upvotes = newUpvotes,
                                downvotes = newDownvotes,
                                upvotedBy = updatedUpvotedBy,
                                downvotedBy = updatedDownvotedBy
                            )
                            postAdapter.updateUpvoteCount(position, newUpvotes)
                            postAdapter.updateDownvoteCount(position, newDownvotes)
                            postAdapter.updateUpvotedBy(position, updatedUpvotedBy)
                            postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
                        }
                    } else {
                        newDownvotes += 1
                        postRef.update(
                            "downvotes", newDownvotes,
                            "downvotedBy", FieldValue.arrayUnion(currentUserUid)
                        ).await()
                        withContext(Dispatchers.Main){
                            val updatedDownvotedBy = (serverDownvotedBy + currentUserUid).toMutableList()
                            postList[position] = post.copy(downvotes = newDownvotes, downvotedBy = updatedDownvotedBy)
                            postAdapter.updateDownvoteCount(position, newDownvotes)
                            postAdapter.updateDownvotedBy(position, updatedDownvotedBy)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main){

                }
            }
        }
    }

    private fun setupPfp() {
        var pfp: Int
        userUid?.let {
            users.whereEqualTo("uid", it)
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
    }

    override fun onItemClick(position: Int) {
        val post = postList[position]
        val intent = Intent(requireContext(), PostActivity::class.java)
        intent.putExtra("postId", post.id)
        startActivity(intent)
    }
}

