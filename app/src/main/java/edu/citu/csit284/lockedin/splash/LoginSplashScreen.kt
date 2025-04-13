package edu.citu.csit284.lockedin.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.MainActivity
import edu.citu.csit284.lockedin.R

class LoginSplashScreen : Activity() {
    private val users = Firebase.firestore.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_splash_screen)

        val imgpfp = findViewById<ImageView>(R.id.pfp)
        val bgImg = findViewById<ImageView>(R.id.bgImage)
        val name = findViewById<TextView>(R.id.name)
        val welcome = findViewById<TextView>(R.id.welcome)

        imgpfp.visibility = View.INVISIBLE
        name.visibility = View.INVISIBLE
        welcome.visibility = View.INVISIBLE

        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val userInfo = sharedPref.getString("username","")
        name.setText(userInfo)
        val fade = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        bgImg.startAnimation(fade)

        users
            .whereEqualTo("username",userInfo)
            .get()
            .addOnSuccessListener {documents ->
                if(!documents.isEmpty){
                    val document = documents.documents[0]
                    val pfp = document.getLong("pfpID")?.toInt() ?: 2
                    when (pfp) {
                        1 -> {
                            imgpfp.setImageResource(R.drawable.red_pfp)
                            name.setTextColor(ContextCompat.getColor(this,R.color.red))
                        }
                        2 -> {
                            imgpfp.setImageResource(R.drawable.default_pfp)
                            name.setTextColor(ContextCompat.getColor(this,R.color.yellow))
                        }
                        3 -> {
                            imgpfp.setImageResource(R.drawable.green_pfp)
                            name.setTextColor(ContextCompat.getColor(this,R.color.green))
                        }
                        4 -> {
                            imgpfp.setImageResource(R.drawable.blue_pfp)
                            name.setTextColor(ContextCompat.getColor(this,R.color.pfpblue))
                        }
                    }
                }
                imgpfp.visibility = View.VISIBLE
                name.visibility = View.VISIBLE
                welcome.visibility = View.VISIBLE

                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
                val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_fade_up)

                imgpfp.startAnimation(fadeIn)
                name.startAnimation(slideUp)
                welcome.startAnimation(slideUp)
            }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}