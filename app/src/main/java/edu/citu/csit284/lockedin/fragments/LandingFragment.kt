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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.activities.MatchDetailsActivity
import edu.citu.csit284.lockedin.activities.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.LoadingAnimationUtil
import edu.citu.csit284.lockedin.util.fetchArticles

class LandingFragment : Fragment() {
    private val users = Firebase.firestore.collection("users")
    private var caller: String? = null
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private lateinit var noInternetBox: LinearLayout
    private lateinit var onGoingMatch: FrameLayout
    private lateinit var header: LinearLayout

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
        onGoingMatch = view.findViewById(R.id.ongoingMatch)
        header = view.findViewById(R.id.header)
        startPulsatingAnimation(onGoingMatch)
        startPulsatingAnimation(header)

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

        val matchDetail = view.findViewById<FrameLayout>(R.id.ongoingMatch)
        matchDetail.setOnClickListener {
            startActivity(Intent(requireContext(), MatchDetailsActivity::class.java))
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LoadingAnimationUtil.cancelAnimations()
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

}