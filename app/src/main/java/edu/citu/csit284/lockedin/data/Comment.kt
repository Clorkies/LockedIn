package edu.citu.csit284.lockedin.data

data class Comment(
    val id: String,
    val authorUsername: String? = null,
    val description: String? = null,
    val timestamp: Long = 0,
)