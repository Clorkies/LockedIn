package edu.citu.csit284.lockedin.data

data class Post(
    val id: String,
    val authorUsername: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    var upvotes: Int = 0,
    var downvotes: Int = 0,
    val timestamp: Long = 0,
    var upvotedBy: MutableList<String> = mutableListOf(),
    var downvotedBy: MutableList<String> = mutableListOf()
)