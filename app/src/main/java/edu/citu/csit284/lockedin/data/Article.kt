package edu.citu.csit284.lockedin.data

import androidx.annotation.DrawableRes

data class Article (
    @DrawableRes var imgResId: Int,
    var title: String = "",
    var articleText: String = ""
)