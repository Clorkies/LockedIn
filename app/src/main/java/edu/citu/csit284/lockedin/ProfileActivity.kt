package edu.citu.csit284.lockedin

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle

class ProfileActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val name = findViewById<EditText>(R.id.name)
        val bio = findViewById<EditText>(R.id.bio)
        val pass = findViewById<EditText>(R.id.password)
        val email = findViewById<EditText>(R.id.email)
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        pass.toggle(imgPriv)
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val editList  = listOf(name,bio,pass)
        val imgpfp = findViewById<ImageView>(R.id.pfp)
        var pfp : Int

        val userInfo = sharedPref.getString("username","")
        users
            .whereEqualTo("username",userInfo)
            .get()
            .addOnSuccessListener {documents ->
                if(!documents.isEmpty){
                    val document = documents.documents[0]
                    pfp = document.getLong("pfpID")?.toInt() ?: 2
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
            }

        users
            .whereEqualTo("username",userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty){
                    for (document in documents) {
                        name.setText(document.getString("username"))
                        pass.setText(document.getString("password"))
                        email.setText(document.getString("email"))
                        if(document.contains("bio")){
                            bio.setText(document.getString("bio"))
                        }
                    }
                }
            }

        val btn_edit = findViewById<Button>(R.id.btn_edit)
        val btn_logout = findViewById<Button>(R.id.button_logout)
        var editIsClicked = false
        btn_edit.setOnClickListener {
            if(btn_edit.text.equals("Edit Information")){
                editIsClicked = true
                btn_edit.setText("Save Changes")
                btn_logout.setText("Cancel")
                imgpfp.setColorFilter(Color.argb(100, 255, 255, 255), PorterDuff.Mode.LIGHTEN)
                for (editText in editList) {
                    editText.setBackgroundResource(R.drawable.white_underline)
                    editText.isFocusable = true
                    editText.isFocusableInTouchMode = true
                    editText.isCursorVisible = true
                    editText.isLongClickable = true
                    editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
                    if(editText == pass){
                        pass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                    if(editText == bio){
                        bio.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }
            }else{
                editIsClicked = false
                imgpfp.clearColorFilter()
                btn_edit.setText("Edit Information")
                btn_logout.setText("Log Out")
                for (editText in editList) {
                    editText.setBackgroundResource(android.R.color.transparent)
                    editText.isFocusable = false
                    editText.isFocusableInTouchMode = false
                    editText.isCursorVisible = false
                    editText.isLongClickable = false
                    editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    if(editText == pass){
                        pass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    if(editText == bio){
                        bio.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }
                val username = name.text.toString().trim()
                val password = pass.text.toString().trim()
                val em = email.text.toString().trim()
                val newBio = bio.text.toString().trim()
                val updatedUser: Map<String, Any> = mapOf (
                    "username" to username,
                    "email" to em,
                    "password" to password,
                    "bio" to newBio
                )
                users
                    .whereEqualTo("email",em)
                    .get()
                    .addOnSuccessListener { documents ->
                        val document = documents.documents[0]
                        val documentId = document.id
                        users.document(documentId)
                            .set(updatedUser, SetOptions.merge())
                            .addOnSuccessListener {
                                toast("Updated Successfully!")
                            }
                    }

            }
        }
        imgpfp.setOnClickListener {
            if(editIsClicked){
                val dialog = BottomSheetDialog(this)
                val view = layoutInflater.inflate(R.layout.profile_picker, null)
                val em = email.text.toString()
                dialog.setContentView(view)

                val option1 = view.findViewById<ImageView>(R.id.option1)
                val option2 = view.findViewById<ImageView>(R.id.option2)
                val option3 = view.findViewById<ImageView>(R.id.option3)
                val option4 = view.findViewById<ImageView>(R.id.option4)
                option1.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.red_pfp)
                    pfp = 1
                    name.setTextColor(ContextCompat.getColor(this,R.color.red))
                    updatePFP(em,pfp)
                    dialog.dismiss()
                }
                option2.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.default_pfp)
                    pfp = 2
                    name.setTextColor(ContextCompat.getColor(this,R.color.yellow))
                    updatePFP(em,pfp)
                    dialog.dismiss()
                }
                option3.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.green_pfp)
                    pfp = 3
                    name.setTextColor(ContextCompat.getColor(this,R.color.green))
                    updatePFP(em,pfp)
                    dialog.dismiss()
                }
                option4.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.blue_pfp)
                    pfp = 4
                    name.setTextColor(ContextCompat.getColor(this,R.color.pfpblue))
                    updatePFP(em,pfp)
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish(); }
        editList.forEach { editText ->
            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)

                    v.clearFocus()

                    true
                } else {
                    false
                }
            }
        }

        val btn_settings = findViewById<ImageButton>(R.id.button_settings)
        btn_settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)); finish(); }

        btn_logout.setOnClickListener {
            if(btn_logout.text.equals("Cancel")){
                btn_edit.setText("Edit Information")
                btn_logout.setText("Log Out")
                imgpfp.clearColorFilter()
                for (editText in editList) {
                    editText.setBackgroundResource(android.R.color.transparent)
                    editText.isFocusable = false
                    editText.isFocusableInTouchMode = false
                    editText.isCursorVisible = false
                    editText.isLongClickable = false
                    editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    if(editText == pass){
                        pass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    if(editText == bio){
                        bio.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }
            }else{
                val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
                val bottom = BottomSheetDialog(this,R.style.BottomSheetDialogTheme)
                bottom.setContentView(sheet)
                val back = sheet.findViewById<Button>(R.id.back_btn)
                back.setOnClickListener {
                    bottom.dismiss()
                }
                val logout = sheet.findViewById<Button>(R.id.logout)
                logout.setOnClickListener {
                    sharedPref.edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                bottom.show()
            }
        }
    }
    private fun updatePFP(email: String, pfpID: Int) {
        users
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val document = documents.documents[0]
                val documentId = document.id
                users.document(documentId).update("pfpID", pfpID)

            }
    }
}