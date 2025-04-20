package edu.citu.csit284.lockedin.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.citu.csit284.lockedin.R

object LoadingAnimationUtils {
    private var animator1: ValueAnimator? = null
    private var animator2: ValueAnimator? = null

    fun showLoading(context: Context, view1: View, view2: View, show: Boolean) {
        if (show) {
            cancelAnimations()

            view1.visibility = View.VISIBLE
            view2.visibility = View.VISIBLE

            val loadingBar1 = view1.findViewById<ImageView>(R.id.loadingBar)
            val loadingBar2 = view2.findViewById<ImageView>(R.id.loadingBar)

            loadingBar1.translationX = -600f
            loadingBar2.translationX = -600f

            view1.post { startTranslationAnimation(loadingBar1, 0) }
            view2.post { startTranslationAnimation(loadingBar2, 300) }
        } else {
            cancelAnimations()

            view1.visibility = View.GONE
            view2.visibility = View.GONE
        }
    }

    private fun startTranslationAnimation(imageView: ImageView, startDelay: Long) {
        imageView.post {
            val parentWidth = (imageView.parent as View).width.toFloat()
            val barWidth = imageView.width.toFloat()

            val startX = -barWidth
            val endX = parentWidth

            val totalDistance = endX - startX

            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1400
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()

                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Float
                    imageView.translationX = startX + (progress * totalDistance)
                }
            }

            if ((imageView.parent.parent as? View)?.id == R.id.loadingView1) {
                animator1 = animator
            } else {
                animator2 = animator
            }

            animator.start()
        }
    }


    fun cancelAnimations() {
        animator1?.cancel()
        animator1 = null
        animator2?.cancel()
        animator2 = null
    }
}

fun startPulsatingAnimation(view: View) {
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

fun setupLeftRightAnimation(view: View, recyclerView: RecyclerView) {
    view.visibility = View.GONE

    val translateAnimation = ObjectAnimator.ofFloat(view, "translationX", 5f, -55f)
    translateAnimation.duration = 1000
    translateAnimation.repeatCount = ValueAnimator.INFINITE
    translateAnimation.repeatMode = ValueAnimator.REVERSE
    translateAnimation.interpolator = AccelerateDecelerateInterpolator()
    translateAnimation.start()

    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                newState == RecyclerView.SCROLL_STATE_SETTLING) {
                view.visibility = View.GONE
                translateAnimation.cancel()
            }
        }
    })
}