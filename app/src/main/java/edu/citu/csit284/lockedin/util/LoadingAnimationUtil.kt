package edu.citu.csit284.lockedin.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import edu.citu.csit284.lockedin.R

object LoadingAnimationUtil {
    private var animator1: ValueAnimator? = null
    private var animator2: ValueAnimator? = null

    fun setupLoadingViews(context: Context, view1: View, view2: View) {
        val cornerRadius = 30f * context.resources.displayMetrics.density

        val drawable1 = GradientDrawable().apply {
            this.cornerRadius = cornerRadius
            setColor(ContextCompat.getColor(context, R.color.loadingstate_dark))
        }

        val drawable2 = GradientDrawable().apply {
            this.cornerRadius = cornerRadius
            setColor(ContextCompat.getColor(context, R.color.loadingstate_bright))
        }

        view1.background = drawable1
        view2.background = drawable2
    }

    fun showLoading(context: Context, activity: Activity, view1: View, view2: View, show: Boolean) {
        if (show) {
            view1.visibility = View.VISIBLE
            view2.visibility = View.VISIBLE

            view1.layoutParams = view1.layoutParams.apply { width = 0 }
            view2.layoutParams = view2.layoutParams.apply { width = 0 }

            startGrowAnimation(context, activity, view1, 0)
            startGrowAnimation(context, activity, view2, 200)
        } else {
            animator1?.cancel()
            animator2?.cancel()

            view1.visibility = View.GONE
            view2.visibility = View.GONE
        }
    }

    private fun startGrowAnimation(context: Context, activity: Activity, view: View, startDelay: Long) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val targetWidth = screenWidth - (40 * context.resources.displayMetrics.density).toInt()

        val animator = ValueAnimator.ofInt(0, targetWidth).apply {
            duration = 700
            this.startDelay = startDelay
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val width = animation.animatedValue as Int
                view.layoutParams = view.layoutParams.apply { this.width = width }
                view.requestLayout()
            }
        }

        if (view.id == view1Id) animator1 = animator else animator2 = animator
        animator.start()
    }

    fun cancelAnimations() {
        animator1?.cancel()
        animator2?.cancel()
    }

    private val view1Id = R.id.loadingView1
}
