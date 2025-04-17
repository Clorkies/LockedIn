package edu.citu.csit284.lockedin.data

data class Post(
    val authorUsername: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val timestamp: Long = 0,
)