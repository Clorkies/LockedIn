package edu.citu.csit284.lockedin.activities

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import edu.citu.csit284.lockedin.R

import edu.citu.csit284.lockedin.util.toast
import java.io.IOException

private const val REQUEST_IMAGE_PICK = 100
class CreatePostActivity : Activity() {
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnPost: Button
    private lateinit var imageContainer : FrameLayout
    private lateinit var imagePreview : ImageView
    private lateinit var btnDelete : ImageView
    private lateinit var footer : LinearLayout
    private var selectedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener { finish() }
        etTitle = findViewById(R.id.title)
        etBody  = findViewById(R.id.body)
        btnPost = findViewById(R.id.buttonPost)
        imageContainer = findViewById(R.id.imageContainer)
        imagePreview = findViewById(R.id.imagePreview)
        btnDelete = findViewById(R.id.buttonDelete)
        footer = findViewById(R.id.footer)

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
        footer.setOnClickListener {
            getImage()
        }
        btnPost.setOnClickListener {
            toast("wauz")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            data.data?.let { uri ->
                selectedImageUri = uri
                displayImagePreview(uri)
            }
        }
    }
    private fun getImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    private fun displayImagePreview(uri: Uri) {
        imageContainer.visibility = View.VISIBLE
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            imagePreview.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
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