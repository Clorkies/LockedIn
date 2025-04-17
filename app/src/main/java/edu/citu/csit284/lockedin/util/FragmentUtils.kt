package edu.citu.csit284.lockedin.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.citu.csit284.lockedin.data.Match

fun setupHeaderScrollBehavior(headerContainer: LinearLayout, listView: ListView) {
    var lastFirstVisibleItem = 0
    var headerHeight = headerContainer.height + 280
    var isAnimating = false
    var isCollapsed = false

    val contentContainer = headerContainer.parent as LinearLayout
    val frameLayout = listView.parent as FrameLayout

    val elementsToMove = mutableListOf<View>()
    for (i in 0 until contentContainer.childCount) {
        val child = contentContainer.getChildAt(i)
        if (child == frameLayout) {
            break
        }
        elementsToMove.add(child)
    }

    if (headerHeight == 0) {
        headerContainer.post {
            headerHeight = headerContainer.height
        }
    }

    val originalFrameParams = LinearLayout.LayoutParams(
        frameLayout.layoutParams.width,
        frameLayout.layoutParams.height
    ).also {
        if (frameLayout.layoutParams is LinearLayout.LayoutParams) {
            val params = frameLayout.layoutParams as LinearLayout.LayoutParams
            it.weight = params.weight
            it.topMargin = params.topMargin
            it.bottomMargin = params.bottomMargin
            it.leftMargin = params.leftMargin
            it.rightMargin = params.rightMargin
            it.gravity = params.gravity
        }
    }

    val heightAnimator = ValueAnimator()
    val marginAnimator = ValueAnimator()

    listView.setOnScrollListener(object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        }

        override fun onScroll(
            view: AbsListView?,
            firstVisibleItem: Int,
            visibleItemCount: Int,
            totalItemCount: Int
        ) {
            if (isAnimating) return

            if (firstVisibleItem > lastFirstVisibleItem && !isCollapsed) {
                heightAnimator.cancel()
                marginAnimator.cancel()

                isAnimating = true
                isCollapsed = true

                elementsToMove.forEach { view ->
                    view.animate()
                        .translationY(-headerHeight.toFloat())
                        .setDuration(150)
                        .start()
                }

                val animatorSet = AnimatorSet()

                val heightAnim = ValueAnimator.ofInt(originalFrameParams.height, originalFrameParams.height + headerHeight)
                heightAnim.addUpdateListener { valueAnimator ->
                    val params = frameLayout.layoutParams
                    params.height = valueAnimator.animatedValue as Int
                    frameLayout.layoutParams = params
                }

                val marginAnim = ValueAnimator.ofInt(0, -headerHeight)
                marginAnim.addUpdateListener { valueAnimator ->
                    val params = frameLayout.layoutParams as LinearLayout.LayoutParams
                    params.topMargin = valueAnimator.animatedValue as Int
                    frameLayout.layoutParams = params
                }

                animatorSet.playTogether(heightAnim, marginAnim)
                animatorSet.duration = 150
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimating = false
                    }
                })
                animatorSet.start()

            } else if (firstVisibleItem < lastFirstVisibleItem && isCollapsed) {
                heightAnimator.cancel()
                marginAnimator.cancel()

                isAnimating = true
                isCollapsed = false

                elementsToMove.forEach { v ->
                    v.animate()
                        .translationY(0f)
                        .setDuration(150)
                        .start()
                }

                val animatorSet = AnimatorSet()

                val currentHeight = frameLayout.height
                val currentMargin = (frameLayout.layoutParams as LinearLayout.LayoutParams).topMargin

                val heightAnim = ValueAnimator.ofInt(currentHeight, originalFrameParams.height)
                heightAnim.addUpdateListener { valueAnimator ->
                    val params = frameLayout.layoutParams
                    params.height = valueAnimator.animatedValue as Int
                    frameLayout.layoutParams = params
                }

                val marginAnim = ValueAnimator.ofInt(currentMargin, 0)
                marginAnim.addUpdateListener { valueAnimator ->
                    val params = frameLayout.layoutParams as LinearLayout.LayoutParams
                    params.topMargin = valueAnimator.animatedValue as Int
                    frameLayout.layoutParams = params
                }

                animatorSet.playTogether(heightAnim, marginAnim)
                animatorSet.duration = 150
                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        frameLayout.layoutParams = getClonedParams(originalFrameParams)
                        isAnimating = false
                    }
                })
                animatorSet.start()
            }

            lastFirstVisibleItem = firstVisibleItem
        }
    })
}

private fun getClonedParams(original: LinearLayout.LayoutParams): LinearLayout.LayoutParams {
    return LinearLayout.LayoutParams(
        original.width,
        original.height
    ).also {
        it.weight = original.weight
        it.topMargin = original.topMargin
        it.bottomMargin = original.bottomMargin
        it.leftMargin = original.leftMargin
        it.rightMargin = original.rightMargin
        it.gravity = original.gravity
    }
}