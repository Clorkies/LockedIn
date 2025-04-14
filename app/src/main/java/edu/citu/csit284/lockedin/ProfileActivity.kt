package edu.citu.csit284.lockedin

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
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
        val passReq = findViewById<LinearLayout>(R.id.passwordRequirements)
        passReq.visibility = View.GONE
        val profileBottomSheet = findViewById<LinearLayout>(R.id.profileBottomSheet)
        profileBottomSheet.translationY = 1330f
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        pass.toggle(imgPriv)
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        val editList  = listOf(name,bio,pass)
        val imgpfp = findViewById<ImageView>(R.id.pfp)
        var pfp : Int

        val btn_edit = findViewById<Button>(R.id.btn_edit)
        val btn_logout = findViewById<Button>(R.id.button_logout)

        // Password Requirements
        val tvPasswordStrength = findViewById<TextView>(R.id.tvPasswordStrength)
        val tvRuleLength = findViewById<TextView>(R.id.tvRuleLength)
        val tvRuleUppercase = findViewById<TextView>(R.id.tvRuleUppercase)
        val tvRuleNumber = findViewById<TextView>(R.id.tvRuleNumber)
        pass.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                var metRules = 0

                val isLengthValid = pass.length >= 8
                val isGreaterThanMin = pass.length > 8
                val hasUppercase = pass.any { it.isUpperCase() }
                val hasNumber = pass.any { it.isDigit() }

                metRules += updateRuleStatus(tvRuleLength, isLengthValid)
                metRules += updateRuleStatus(tvRuleUppercase, hasUppercase)
                metRules += updateRuleStatus(tvRuleNumber, hasNumber)
                metRules += if (isGreaterThanMin) 1 else 0

                when (metRules) {
                    0, 1 -> {
                        tvPasswordStrength.text = "Weak"
                        btn_edit.isEnabled = false
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.red))
                        btn_edit.setBackgroundResource(R.drawable.btn_register_disabled)
                    }
                    2 -> {
                        tvPasswordStrength.text = "Good"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.devYellow))
                        btn_edit.isEnabled = isLengthValid
                        if (isLengthValid) btn_edit.setBackgroundResource(R.drawable.btn_register)
                    }
                    3 -> {
                        tvPasswordStrength.text = "Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.teal_700))
                        btn_edit.isEnabled = isLengthValid
                        if (isLengthValid) btn_edit.setBackgroundResource(R.drawable.btn_register)
                    }
                    4 -> {
                        tvPasswordStrength.text = "Very Strong"
                        tvPasswordStrength.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.purple_500))
                        btn_edit.isEnabled = isGreaterThanMin
                        if (isGreaterThanMin) btn_edit.setBackgroundResource(R.drawable.btn_register)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            private fun updateRuleStatus(textView: TextView, isValid: Boolean): Int {
                val color = if (!isValid) R.color.passwordReqs else R.color.devYellow
                textView.setTextColor(ContextCompat.getColor(this@ProfileActivity, color))
                return if (isValid) 1 else 0
            }
        })


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

        imgpfp.setOnClickListener {
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

        btn_edit.setOnClickListener {
            if(btn_edit.text.equals("Edit Information")){
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
                profileBottomSheet.translationY = 1330f-136f

                passReq.visibility = View.VISIBLE

                profileBottomSheet.post {
                    profileBottomSheet.requestLayout()
                    profileBottomSheet.invalidate()

                    profileBottomSheet.animate()
                        .translationY(1050f)
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }


            }else{
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

                passReq.visibility = View.GONE

                profileBottomSheet.translationY = 1050+136f


                profileBottomSheet.post {
                    profileBottomSheet.requestLayout()
                    profileBottomSheet.invalidate()

                    profileBottomSheet.animate()
                        .translationY(1330f)
                        .setDuration(300)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }
        }
        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish(); }

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