package edu.citu.csit284.lockedin.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.util.toast
import edu.citu.csit284.lockedin.util.toggle
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.AuthCredential

class ProfileActivity : Activity() {
    private val users = Firebase.firestore.collection("users")
    lateinit var auth: FirebaseAuth
    var origName: String? = ""
    var origBio: String? = ""
    var origPass: String? = ""
    lateinit var userInfo: String
    var editIsClicked: Boolean = false
    lateinit var btn_edit: Button
    lateinit var btn_logout: Button
    lateinit var pass: EditText
    lateinit var bio: EditText
    lateinit var editList: MutableList<EditText>
    lateinit var emailEditText: EditText
    lateinit var mReauthEmail: String
    lateinit var mReauthPass: String
    lateinit var sharedPref: SharedPreferences

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("User", MODE_PRIVATE)

        val nameEditText = findViewById<EditText>(R.id.name)
        bio = findViewById(R.id.bio)
        pass = findViewById(R.id.password)
        emailEditText = findViewById(R.id.email)
        val passReq = findViewById<LinearLayout>(R.id.passwordRequirements)
        passReq.visibility = View.GONE
        val profileBottomSheet = findViewById<LinearLayout>(R.id.profileBottomSheet)
        profileBottomSheet.translationY = 1330f
        val imgPriv = findViewById<ImageView>(R.id.imgPriv)
        imgPriv.visibility = View.GONE
        pass.toggle(imgPriv)
        val sharedPref = getSharedPreferences("User", MODE_PRIVATE)
        editList = mutableListOf(nameEditText, this.bio)

        editList.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.setSelection(editText.text.length)
                }
            }
        }
        editList.add(pass)
        val imgpfp = findViewById<ImageView>(R.id.pfp)
        var pfp: Int

        btn_edit = findViewById(R.id.btn_edit)
        btn_logout = findViewById(R.id.button_logout)

        val tvPasswordStrength = findViewById<TextView>(R.id.tvPasswordStrength)
        val tvRuleLength = findViewById<TextView>(R.id.tvRuleLength)
        val tvRuleUppercase = findViewById<TextView>(R.id.tvRuleUppercase)
        val tvRuleNumber = findViewById<TextView>(R.id.tvRuleNumber)


        userInfo = sharedPref.getString("username", "").toString()
        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        nameEditText.setText(document.getString("username"))
                        pass.setText("Enter new password")
                        emailEditText.setText(document.getString("email"))
                        origName = nameEditText.text.toString()
                        origPass = pass.text.toString()

                        if (document.contains("bio")) {
                            this.bio.setText(document.getString("bio"))
                            origBio = this.bio.text.toString()
                        }
                    }
                }
            }
        pass.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val passText = s.toString()
                var metRules = 0

                val isLengthValid = passText.length >= 8
                val isGreaterThanMin = passText.length > 8
                val hasUppercase = passText.any { it.isUpperCase() }
                val hasNumber = passText.any { it.isDigit() }

                metRules += updateRuleStatus(tvRuleLength, isLengthValid)
                metRules += updateRuleStatus(tvRuleUppercase, hasUppercase)
                metRules += updateRuleStatus(tvRuleNumber, hasNumber)
                metRules += if (isGreaterThanMin) 1 else 0

                when (metRules) {
                    0, 1 -> {
                        tvPasswordStrength.text = "Weak"
                        btn_edit.isEnabled = false
                        tvPasswordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@ProfileActivity,
                                R.color.red
                            )
                        )
                        btn_edit.setBackgroundResource(R.drawable.btn_register_disabled)
                    }
                    2 -> {
                        tvPasswordStrength.text = "Good"
                        tvPasswordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@ProfileActivity,
                                R.color.devYellow
                            )
                        )
                        btn_edit.isEnabled = isLengthValid
                        if (isLengthValid) btn_edit.setBackgroundResource(R.drawable.btn_register)
                    }
                    3 -> {
                        tvPasswordStrength.text = "Strong"
                        tvPasswordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@ProfileActivity,
                                R.color.teal_700
                            )
                        )
                        btn_edit.isEnabled = isLengthValid
                        if (isLengthValid) btn_edit.setBackgroundResource(R.drawable.btn_register)
                    }
                    4 -> {
                        tvPasswordStrength.text = "Very Strong"
                        tvPasswordStrength.setTextColor(
                            ContextCompat.getColor(
                                this@ProfileActivity,
                                R.color.purple_500
                            )
                        )
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

        users
            .whereEqualTo("username", userInfo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    pfp = document.getLong("pfpID")?.toInt() ?: 2
                    when (pfp) {
                        1 -> {
                            imgpfp.setImageResource(R.drawable.red_pfp)
                            nameEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
                        }
                        2 -> {
                            imgpfp.setImageResource(R.drawable.default_pfp)
                            nameEditText.setTextColor(ContextCompat.getColor(this, R.color.yellow))
                        }
                        3 -> {
                            imgpfp.setImageResource(R.drawable.green_pfp)
                            nameEditText.setTextColor(ContextCompat.getColor(this, R.color.green))
                        }
                        4 -> {
                            imgpfp.setImageResource(R.drawable.blue_pfp)
                            nameEditText.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.pfpblue
                                )
                            )
                        }
                    }
                }
            }

        editIsClicked = false
        pass.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pass.setSelection(pass.text.length)
                profileBottomSheet.translationY = 1330f - 136f

                passReq.visibility = View.VISIBLE

                profileBottomSheet.post {
                    profileBottomSheet.requestLayout()
                    profileBottomSheet.invalidate()

                    profileBottomSheet.animate()
                        .translationY(1000f)
                        .setDuration(300)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()

                }
            } else {
                passReq.visibility = View.GONE

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
        btn_edit.setOnClickListener {
            if (btn_edit.text.equals("Edit Information")) {
                editIsClicked = true
                btn_edit.setText("Save Changes")
                btn_logout.setText("Cancel")
                imgPriv.visibility = View.VISIBLE
                imgpfp.setColorFilter(Color.argb(100, 255, 255, 255), PorterDuff.Mode.LIGHTEN)
                for (editText in editList) {
                    editText.setBackgroundResource(R.drawable.settings_background)
                    editText.isFocusable = true
                    editText.isFocusableInTouchMode = true
                    editText.isCursorVisible = true
                    editText.isLongClickable = true
                    editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
                    if (editText == pass) {
                        pass.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                    if (editText == bio) {
                        bio.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }

            } else {
                editIsClicked = false
                imgpfp.clearColorFilter()
                btn_edit.setText("Edit Information")
                btn_logout.setText("Log Out")
                imgPriv.visibility = View.GONE
                for (editText in editList) {
                    editText.setBackgroundResource(android.R.color.transparent)
                    editText.isFocusable = false
                    editText.isFocusableInTouchMode = false
                    editText.isCursorVisible = false
                    editText.isLongClickable = false
                    editText.inputType =
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    if (editText == pass) {
                        pass.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    if (editText == bio) {
                        bio.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }
                val newName = nameEditText.text.toString().trim()
                val newPass = pass.text.toString().trim()
                val em = emailEditText.text.toString().trim()
                val newBio = bio.text.toString().trim()

                users.whereEqualTo("username", newName)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty || newName == origName) {
                            if (newPass != "Enter new password") {
                                val user = auth.currentUser
                                if (user != null) {
                                    user.updatePassword(newPass)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(
                                                    "ProfileActivity",
                                                    "Password updated successfully."
                                                )
                                                updateFirestore(em, newName, newBio)
                                            } else {
                                                if (task.exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                                    toast("Re-authentication required. Please log in again.")
                                                    mReauthEmail = em
                                                    mReauthPass = newPass
                                                    showReauthenticationDialog()
                                                } else {
                                                    toast("Failed to update password. Please try again.")
                                                    btn_edit.setText("Edit Information")
                                                    btn_edit.isEnabled = true
                                                }
                                            }
                                        }
                                } else {
                                    toast("No user logged in.")
                                    btn_edit.setText("Edit Information")
                                    btn_edit.isEnabled = true
                                }

                            } else {
                                updateFirestore(em, newName, newBio)
                            }


                        } else {
                            toast("Username already exists. Please choose a different one.")
                            nameEditText.setText(origName)
                            btn_edit.setText("Edit Information")
                            btn_edit.isEnabled = true
                        }
                    }
                    .addOnFailureListener { e ->
                        toast("Error checking username!")
                        btn_edit.setText("Edit Information")
                        btn_edit.isEnabled = true
                    }
            }
        }
        imgpfp.setOnClickListener {
            if (editIsClicked) {
                val dialog = BottomSheetDialog(this)
                val view = layoutInflater.inflate(R.layout.profile_picker, null)
                val em = emailEditText.text.toString()
                dialog.setContentView(view)

                val option1 = view.findViewById<ImageView>(R.id.option1)
                val option2 = view.findViewById<ImageView>(R.id.option2)
                val option3 = view.findViewById<ImageView>(R.id.option3)
                val option4 = view.findViewById<ImageView>(R.id.option4)
                option1.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.red_pfp)
                    pfp = 1
                    nameEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
                    updatePFP(em, pfp)
                    dialog.dismiss()
                }
                option2.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.default_pfp)
                    pfp = 2
                    nameEditText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.yellow
                        )
                    )
                    updatePFP(em, pfp)
                    dialog.dismiss()
                }
                option3.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.green_pfp)
                    pfp = 3
                    nameEditText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.green
                        )
                    )
                    updatePFP(em, pfp)
                    dialog.dismiss()
                }
                option4.setOnClickListener {
                    imgpfp.setImageResource(R.drawable.blue_pfp)
                    pfp = 4
                    nameEditText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.pfpblue
                        )
                    )
                    updatePFP(em, pfp)
                    dialog.dismiss()
                }
                dialog.show()
                passReq.visibility = View.GONE

                profileBottomSheet.translationY = 1050 + 136f

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

        val btn_back = findViewById<ImageButton>(R.id.button_back)
        btn_back.setOnClickListener { finish() }

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

        btn_logout.setOnClickListener {
            if (btn_logout.text.equals("Cancel")) {
                btn_edit.setText("Edit Information")
                btn_logout.setText("Log Out")
                nameEditText.setText(origName)
                if (origBio == "") this.bio.setText("Add a bio!")
                else this.bio.setText(origBio)
                pass.setText(origPass)
                imgpfp.clearColorFilter()
                for (editText in editList) {
                    editText.setBackgroundResource(android.R.color.transparent)
                    editText.isFocusable = false
                    editText.isFocusableInTouchMode = false
                    editText.isCursorVisible = false
                    editText.isLongClickable = false
                    editText.inputType =
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    if (editText == pass) {
                        pass.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    if (editText == bio) {
                        this.bio.inputType =
                            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }
                }
            } else {
                val sheet = LayoutInflater.from(this).inflate(R.layout.logout_bottom_sheet, null)
                val bottom = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
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

    private fun updateFirestore(email: String, newName: String, newBio: String) {
        users.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val documentId = document.id
                    val updatedUser: Map<String, Any> = mapOf(
                        "username" to newName,
                        "email" to email,
                        "bio" to newBio
                    )
                    users.document(documentId)
                        .set(updatedUser, SetOptions.merge())
                        .addOnSuccessListener {
                            toast("Updated Successfully!")
                            origName = newName
                            origBio = newBio
                            editIsClicked = false
                            btn_edit.setText("Edit Information")
                            btn_logout.setText("Log Out")
                            for (editText in editList) {
                                editText.setBackgroundResource(android.R.color.transparent)
                                editText.isFocusable = false
                                editText.isFocusableInTouchMode = false
                                editText.isCursorVisible = false
                                editText.isLongClickable = false
                                editText.inputType =
                                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                if (editText == pass) {
                                    pass.inputType =
                                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                                }
                                if (editText == bio) {
                                    this.bio.inputType =
                                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                }
                            }
                            val editor = sharedPref.edit()
                            editor.putString("username", newName)
                            editor.apply()
                        }
                        .addOnFailureListener { e ->
                            toast("Failed to update profile!")
                            btn_edit.setText("Edit Information")
                            btn_edit.isEnabled = true
                        }
                } else {
                    toast("User not found.")
                    btn_edit.setText("Edit Information")
                    btn_edit.isEnabled = true
                }
            }
    }

    private fun showReauthenticationDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.reauthentication_bottom_sheet, null)
        dialog.setContentView(view)

        val emailEditText = view.findViewById<EditText>(R.id.reauth_email)
        val passwordEditText = view.findViewById<EditText>(R.id.reauth_password)
        val confirmButton = view.findViewById<Button>(R.id.reauth_confirm)
        val cancelButton = view.findViewById<Button>(R.id.reauth_cancel)

        emailEditText.setText(mReauthEmail)
        confirmButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill up all fields.")
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileActivity", "User re-authenticated successfully.")
                    dialog.dismiss()
                    auth.currentUser?.updatePassword(mReauthPass)
                        ?.addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                toast("Password updated!")
                            }
                        }
                } else {
                    toast("Invalid credentials.")
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}

