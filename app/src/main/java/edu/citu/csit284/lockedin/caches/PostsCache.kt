package edu.citu.csit284.lockedin.caches

import edu.citu.csit284.lockedin.data.Post

object PostsCache {
    private val cache = mutableMapOf<String, List<Post>>()
    fun getPostsFor(post: String): List<Post>? {
        return cache[post]
    }
    fun storePostsFor(post: String, matches: List<Post>) {
        cache[post] = matches
    }
    fun clearPostCache(post: String) {
        cache.remove(post)
    }
    fun clearAll() {
        cache.clear()
    }
}