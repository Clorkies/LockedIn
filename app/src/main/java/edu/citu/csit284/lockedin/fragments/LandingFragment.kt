package edu.citu.csit284.lockedin.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.MatchDetailsActivity
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Match
import edu.citu.csit284.lockedin.helper.LiveMatchAdapter
import edu.citu.csit284.lockedin.helper.UpcomingMatchAdapter
import edu.citu.csit284.lockedin.util.LoadingAnimationUtil
import edu.citu.csit284.lockedin.util.MatchRepository
import edu.citu.csit284.lockedin.util.fetchArticles
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

    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var header: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LiveMatchAdapter
    private val matches = mutableListOf<Match>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView1 = view.findViewById(R.id.loadingView1)
        loadingView2 = view.findViewById(R.id.loadingView2)
        noInternetBox = view.findViewById(R.id.noInternetBox)
        noInternetBox.visibility = View.GONE
        header = view.findViewById(R.id.header)
        recyclerView = view.findViewById(R.id.rvView)
        adapter = LiveMatchAdapter(matches)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        recyclerView.visibility = View.GONE
        startPulsatingAnimation(header)
        startPulsatingAnimation(recyclerView)


        LoadingAnimationUtil.setupLoadingViews(requireContext(), loadingView1, loadingView2)
        LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, true)

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
            requireActivity().supportFragmentManager.popBackStack()
        }
        val sharedPref = requireActivity().getSharedPreferences("User", Activity.MODE_PRIVATE)
        val userInfo = sharedPref.getString("username","")
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

        val listView = view.findViewById<ListView>(R.id.articleListView)

        fetchArticles(requireContext(), listView, caller = "landing") { hasInternet ->
            LoadingAnimationUtil.showLoading(requireContext(), requireActivity(), loadingView1, loadingView2, false)
            noInternetBox.visibility = if (!hasInternet) View.VISIBLE else View.GONE
        }
        loadMatches()

    }
    override fun onDestroy() {
        super.onDestroy()
        LoadingAnimationUtil.cancelAnimations()
        coroutineScope.cancel()
    }

    private fun startPulsatingAnimation(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.05f)
        val scaleUpY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.05f)
        val scaleDownX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.05f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.05f, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            duration = 950
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            duration = 950
            interpolator = AccelerateDecelerateInterpolator()
        }

        val pulseAnimator = AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    start()
                }
            })
        }

        pulseAnimator.start()
    }
    private fun loadMatches() {
        coroutineScope.launch {
            val liveMatches = withContext(Dispatchers.IO) {
                    matchRepository.getLiveMatches()
                }
            recyclerView.visibility = View.VISIBLE
            recyclerView.scrollToPosition(0)
            matches.clear()
            matches.addAll(liveMatches)
            adapter.notifyDataSetChanged()

        }
    }

}