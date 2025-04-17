package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.toast

class CreatePostActivity : Activity() {
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnPost: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener { finish() }
        etTitle = findViewById(R.id.title)
        etBody  = findViewById(R.id.body)
        btnPost = findViewById(R.id.buttonPost)
        btnPost.backgroundTintList = ContextCompat.getColorStateList(this, R.color.settingsbg)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkPost()
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        etTitle.addTextChangedListener(textWatcher)
        etBody.addTextChangedListener(textWatcher)
        btnPost.setOnClickListener {
            toast("wauz")
        }
    }
    private fun checkPost() {
        val titleText = etTitle.text.toString().trim()
        val bodyText = etBody.text.toString().trim()

        val isTitleValid = titleText.isNotEmpty()
        val isBodyValid = bodyText.isNotEmpty()

        if (isTitleValid && isBodyValid) {
            btnPost.isEnabled = true
            btnPost.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnPost.backgroundTintList = ContextCompat.getColorStateList(this, R.color.yellow)
        } else {
            btnPost.isEnabled = false
            btnPost.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnPost.backgroundTintList = ContextCompat.getColorStateList(this, R.color.settingsbg)
        }
    }
}