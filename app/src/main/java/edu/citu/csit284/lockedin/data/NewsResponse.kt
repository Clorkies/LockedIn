package edu.citu.csit284.lockedin.data

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)