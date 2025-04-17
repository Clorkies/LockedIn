package edu.citu.csit284.lockedin.util

import android.view.View
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView

fun setupHeaderScrollBehavior(headerContainer: LinearLayout, listView: ListView) {
    var lastFirstVisibleItem = 0
    var headerHeight = headerContainer.height + 270
    var isAnimating = false

    val contentContainer = headerContainer.parent as LinearLayout
    val frameLayout = listView.parent as FrameLayout

    val elementsToMove = mutableListOf<View>()
    for (i in 0 until contentContainer.indexOfChild(frameLayout)) {
        elementsToMove.add(contentContainer.getChildAt(i))
    }

    if (headerHeight == 0) {
        headerContainer.post {
            headerHeight = headerContainer.height
        }
    }

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

            if (firstVisibleItem > lastFirstVisibleItem) {
                isAnimating = true

                elementsToMove.forEach { view ->
                    view.animate()
                        .translationY(-headerHeight.toFloat())
                        .setDuration(150)
                        .start()
                }

                frameLayout.animate()
                    .translationY(-headerHeight.toFloat())
                    .setDuration(150)
                    .withEndAction {
                        isAnimating = false
                    }
                    .start()

            } else if (firstVisibleItem < lastFirstVisibleItem) {
                isAnimating = true

                elementsToMove.forEach { view ->
                    view.animate()
                        .translationY(0f)
                        .setDuration(150)
                        .start()
                }

                frameLayout.animate()
                    .translationY(0f)
                    .setDuration(150)
                    .withEndAction {
                        isAnimating = false
                    }
                    .start()
            }

            lastFirstVisibleItem = firstVisibleItem
        }
    })
}