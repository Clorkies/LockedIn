package edu.citu.csit284.lockedin.data

import edu.citu.csit284.lockedin.R

data class Article(
    val imgResId: Int = R.drawable.img_placeholder,
    val title: String = "No title available",
    val articleText: String = "No description available"
)
