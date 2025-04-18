package edu.citu.csit284.lockedin.util

import android.app.Activity
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

fun EditText.toggle(icon: ImageView) {
    icon.setOnClickListener {
        if (this.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        this.setSelection(this.text.length)
    }
}
fun Activity.toast(msg : String) {
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}