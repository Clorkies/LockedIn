package edu.citu.csit284.lockedin.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.activities.MainActivity
import edu.citu.csit284.lockedin.activities.SettingsActivity
import edu.citu.csit284.lockedin.data.Match
import edu.citu.csit284.lockedin.helper.LiveMatchAdapter
import edu.citu.csit284.lockedin.util.LoadingAnimationUtils
import edu.citu.csit284.lockedin.util.MatchRepository
import edu.citu.csit284.lockedin.util.fetchArticles
import edu.citu.csit284.lockedin.util.setupHeaderScrollBehavior
import edu.citu.csit284.lockedin.util.setupLeftRightAnimation
import edu.citu.csit284.lockedin.util.startPulsatingAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingFragment : Fragment() {
    private val users = Firebase.firestore.collection("users")
    private var caller: String? = null
    private val matchRepository = MatchRepository()

    private lateinit var listView: ListView
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var header: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var rvBackground: ImageView

    private lateinit var pointerSwipeLeft: ImageView
    private lateinit var liveMatchesContainer: FrameLayout

    private lateinit var noMatches : TextView
    private lateinit var adapter: LiveMatchAdapter
    private lateinit var headerContainer: LinearLayout
    private val matches = mutableListOf<Match>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val games = listOf(1 to "valorant", 2 to "lol", 3 to "csgo", 4 to "dota2", 5 to "mlbb", 6 to "overwatch")
    private val gameMap: Map<Int, String> = games.toMap()
    private lateinit var sharedPref: SharedPreferences
    private var userInfo: String? = null
    private var prefNames: List<String> = emptyList()

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
        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty()) {
                    val document = documents.documents[0]
                    val rawList = document.get("favGames") as? List<Long>
                    prefNames = rawList
                        ?.map { it.toInt() }
                        ?.mapNotNull { gameMap[it] }
                        ?: emptyList()
                    loadMatches(prefNames.getOrNull(0)?:"valorant",prefNames.getOrNull(1)?:"valorant",prefNames.getOrNull(2)?:"valorant")
                }
            }
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView1 = view.findViewById(R.id.loadingView1)
        loadingView2 = view.findViewById(R.id.loadingView2)
        noInternetBox = view.findViewById(R.id.noInternetBox)
        noInternetBox.visibility = View.GONE
        noMatches = view.findViewById(R.id.noLiveMatchesTextView)
        header = view.findViewById(R.id.header)
        recyclerView = view.findViewById(R.id.rvView)
        rvBackground = view.findViewById(R.id.rvBackground)

        liveMatchesContainer = view.findViewById(R.id.liveMatchesContainer)
        pointerSwipeLeft = view.findViewById(R.id.pointerSwipeLeft)
        pointerSwipeLeft.visibility = View.GONE
        setupLeftRightAnimation(pointerSwipeLeft, recyclerView)

        adapter = LiveMatchAdapter(matches)
        listView = view.findViewById(R.id.articleListView)
        headerContainer = view.findViewById(R.id.headerContainer)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        liveMatchesContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
        rvBackground.visibility = View.GONE
        startPulsatingAnimation(header)
        startPulsatingAnimation(recyclerView)

        LoadingAnimationUtils.showLoading(requireContext(), loadingView1, loadingView2, true)
        val btnSettings = view.findViewById<ImageButton>(R.id.button_settings)
        btnSettings.setOnClickListener {
            settingsActivityLauncher.launch(Intent(requireContext(), SettingsActivity::class.java))
        }

        listView = view.findViewById(R.id.articleListView)

        loadMatches()

        setupHeaderScrollBehavior(headerContainer, listView, 500)
    }
    override fun onDestroy() {
        super.onDestroy()
        LoadingAnimationUtils.cancelAnimations()
        coroutineScope.cancel()
    }

    private val settingsActivityLauncher = registerForActivityResult(
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

    private fun loadMatches(vararg games: String) {
        coroutineScope.launch {
            val liveMatches = withContext(Dispatchers.IO) {
                matchRepository.getLiveMatches(*games)
            }

            if(liveMatches.isEmpty()){
                liveMatchesContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                rvBackground.visibility = View.GONE
                noMatches.visibility = View.VISIBLE
            } else {
                if(liveMatches.size> 1) {
                    pointerSwipeLeft.visibility = View.VISIBLE
                }else{
                    pointerSwipeLeft.visibility = View.GONE
                }
                liveMatchesContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
                rvBackground.visibility = View.VISIBLE
                recyclerView.scrollToPosition(0)
                matches.clear()
                matches.addAll(liveMatches)
                adapter.notifyDataSetChanged()
            }
            fetchArticles(requireContext(), listView, caller = "landing") { hasInternet ->
                LoadingAnimationUtils.showLoading(requireContext(), loadingView1, loadingView2, false)
                noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
            }
        }
    }

}