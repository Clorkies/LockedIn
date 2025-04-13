package edu.citu.csit284.lockedin.fragments

import android.animation.ValueAnimator
import android.util.DisplayMetrics
import android.view.animation.AccelerateDecelerateInterpolator
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import androidx.core.content.ContextCompat
import edu.citu.csit284.lockedin.MatchDetailsActivity
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.fetchArticles

class LandingFragment : Fragment() {

    private var caller: String? = null
    private lateinit var loadingView1: View
    private lateinit var loadingView2: View
    private var loadingAnimator1: ValueAnimator? = null
    private var loadingAnimator2: ValueAnimator? = null

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

        setupLoadingAnimation()

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        val listView = view.findViewById<ListView>(R.id.articleListView)

        showLoading(true)

        fetchArticles(requireContext(), listView, caller = "landing") {
            showLoading(false)
        }

        val matchDetail = view.findViewById<FrameLayout>(R.id.ongoingMatch)
        matchDetail.setOnClickListener {
            startActivity(Intent(requireContext(), MatchDetailsActivity::class.java))
        }
    }

    private fun setupLoadingAnimation() {
        val drawable1 = GradientDrawable()
        drawable1.cornerRadius = 30f * resources.displayMetrics.density
        drawable1.setColor(ContextCompat.getColor(requireContext(), R.color.loadingstate_dark))

        val drawable2 = GradientDrawable()
        drawable2.cornerRadius = 30f * resources.displayMetrics.density
        drawable2.setColor(ContextCompat.getColor(requireContext(), R.color.loadingstate_bright))

        loadingView1.background = drawable1
        loadingView2.background = drawable2
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingView1.visibility = View.VISIBLE
            loadingView2.visibility = View.VISIBLE

            val params1 = loadingView1.layoutParams
            params1.width = 0
            loadingView1.layoutParams = params1

            val params2 = loadingView2.layoutParams
            params2.width = 0
            loadingView2.layoutParams = params2

            startGrowAnimation(loadingView1, 0)
            startGrowAnimation(loadingView2, 200)
        } else {
            loadingAnimator1?.cancel()
            loadingAnimator2?.cancel()

            loadingView1.visibility = View.GONE
            loadingView2.visibility = View.GONE
        }
    }

    private fun startGrowAnimation(view: View, startDelay: Long) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val targetWidth = screenWidth - (40 * resources.displayMetrics.density).toInt()

        val animator = ValueAnimator.ofInt(0, targetWidth).apply {
            duration = 700
            this.startDelay = startDelay
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val width = animation.animatedValue as Int
                val params = view.layoutParams
                params.width = width
                view.layoutParams = params
            }
        }

        if (view == loadingView1) {
            loadingAnimator1 = animator
        } else {
            loadingAnimator2 = animator
        }

        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingAnimator1?.cancel()
        loadingAnimator2?.cancel()
    }
}


