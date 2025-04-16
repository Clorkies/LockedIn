package edu.citu.csit284.lockedin.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import edu.citu.csit284.lockedin.R

class AboutDevActivity : Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_dev)

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener { finish() }

        val imgDev = findViewById<ImageView>(R.id.imgDev)
        val name = findViewById<TextView>(R.id.name)
        val hobby = findViewById<TextView>(R.id.hobby)
        val fact = findViewById<TextView>(R.id.fact)
        val clarkImg = findViewById<ImageView>(R.id.Clark)
        val jervImg = findViewById<ImageView>(R.id.Jervin)
        val clarkName = findViewById<TextView>(R.id.clarkName)
        val jervName = findViewById<TextView>(R.id.jervName)

        val origName = name.text
        val origHobby = hobby.text
        val origFact = fact.text

        val bottomSheetContainer = findViewById<LinearLayout>(R.id.devsBottomSheet)
        val moreInfoArrow = findViewById<ImageView>(R.id.moreInfoArrow)

        bottomSheetContainer.translationY = 1900f
        var isBottomSheetExpanded = false

        var arrowOffset = 10f
        var positionOffset = 0f

        var arrowBounceAnimation = ValueAnimator.ofFloat(-arrowOffset + positionOffset - 10f, arrowOffset + positionOffset - 10f)
        arrowBounceAnimation.duration = 800
        arrowBounceAnimation.repeatCount = ValueAnimator.INFINITE
        arrowBounceAnimation.repeatMode = ValueAnimator.REVERSE
        arrowBounceAnimation.interpolator = LinearInterpolator()
        arrowBounceAnimation.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            moreInfoArrow.translationY = value
        }
        arrowBounceAnimation.start()

        moreInfoArrow.setOnClickListener {
            val slideAnimation: ObjectAnimator
            if (!isBottomSheetExpanded) {
                slideAnimation = ObjectAnimator.ofFloat(bottomSheetContainer, "translationY", 1900f, 0f)
                moreInfoArrow.scaleY = 1f
                positionOffset -= 140f
            } else {
                slideAnimation = ObjectAnimator.ofFloat(bottomSheetContainer, "translationY", 0f, 1900f)
                moreInfoArrow.scaleY = -1f
                positionOffset += 140f
            }

            arrowBounceAnimation = ValueAnimator.ofFloat(-arrowOffset + positionOffset - 10f, arrowOffset + positionOffset - 10f)
            arrowBounceAnimation.duration = 800
            arrowBounceAnimation.repeatCount = ValueAnimator.INFINITE
            arrowBounceAnimation.repeatMode = ValueAnimator.REVERSE
            arrowBounceAnimation.interpolator = LinearInterpolator()
            arrowBounceAnimation.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                moreInfoArrow.translationY = value
            }
            arrowBounceAnimation.start()

            slideAnimation.duration = 1100
            slideAnimation.interpolator = DecelerateInterpolator()
            slideAnimation.start()
            isBottomSheetExpanded = !isBottomSheetExpanded
        }

        clarkImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()

        jervImg.setOnClickListener {
            imgDev.setImageResource(R.drawable.imgdev2square)
            name.text = "Jervin Milleza"
            hobby.text = "Trades casually"
            fact.text = "6'4 and nonchalant"
            clarkName.setTextColor(ContextCompat.getColor(this, R.color.white))
            jervName.setTextColor(ContextCompat.getColor(this, R.color.yellow))
            jervImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            clarkImg.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }
        clarkImg.setOnClickListener {
            imgDev.setImageResource(R.drawable.imgdev1square)
            name.text = origName
            hobby.text = origHobby
            fact.text = origFact
            clarkName.setTextColor(ContextCompat.getColor(this, R.color.yellow))
            jervName.setTextColor(ContextCompat.getColor(this, R.color.white))
            clarkImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            jervImg.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }
    }
}