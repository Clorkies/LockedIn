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
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.MatchDetailsActivity
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Match
import edu.citu.csit284.lockedin.helper.MatchAdapter
import edu.citu.csit284.lockedin.util.MatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamesFragment : Fragment() {

    private var caller: String? = null
    private val users = Firebase.firestore.collection("users")
    private val matchRepository = MatchRepository()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MatchAdapter
    private val matches = mutableListOf<Match>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var prefGamesInt: MutableList<Int> = mutableListOf()
    private val games = listOf(
        1 to "Valorant",
        2 to "League",
        3 to "CS:GO",
        4 to "Dota",
        5 to "Rivals",
        6 to "Overwatch"
    )
    private val gameMap: Map<Int, String> = games.toMap()
    private lateinit var tvGame1 : TextView
    private lateinit var tvGame2 : TextView
    private lateinit var tvGame3 : TextView
    private lateinit var sharedPref: SharedPreferences
    private var userInfo: String? = null

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
        val root = inflater.inflate(R.layout.fragment_games, container, false)

        tvGame1 = root.findViewById(R.id.game1)
        tvGame2 = root.findViewById(R.id.game2)
        tvGame3 = root.findViewById(R.id.game3)

        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty()) {
                    val document = documents.documents[0]
                    val rawList = document.get("favGames") as? List<Long>
                    val prefNames = rawList
                        ?.map { it.toInt() }
                        ?.mapNotNull { gameMap[it] }
                        ?: emptyList()
                    tvGame1.text = prefNames.getOrNull(0)?.firstOrNull()?.toString() ?: ""
                    tvGame2.text = prefNames.getOrNull(1)?.firstOrNull()?.toString() ?: ""
                    tvGame3.text = prefNames.getOrNull(2)?.firstOrNull()?.toString() ?: ""
                }
            }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvView)
        loadMatches()
        adapter = MatchAdapter(matches)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
            requireActivity().supportFragmentManager.popBackStack()
        }
        var pfp : Int
        users
            .whereEqualTo("username",userInfo)
            .get()
            .addOnSuccessListener {documents ->
                if(!documents.isEmpty){
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

        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "live" -> findNavController().navigate(R.id.liveFragment)
                "explore" -> {}
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }


        btnProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java).apply {
                putExtra("caller", "game")
            }
            startActivity(intent)
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }
    private fun loadMatches() {
        coroutineScope.launch {

            val upcomingMatches = withContext(Dispatchers.IO) {
                matchRepository.getUpcomingValorantMatches()
            }
            matches.clear()
            matches.addAll(upcomingMatches)
            adapter.notifyDataSetChanged()

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
