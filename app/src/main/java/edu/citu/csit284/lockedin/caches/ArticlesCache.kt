package edu.citu.csit284.lockedin.caches

import edu.citu.csit284.lockedin.data.Article

object ArticlesCache {
    private val cache = mutableMapOf<String, List<Article>>()

    fun getArticlesFor(category: String): List<Article>? {
        return cache[category]
    }

    fun storeArticlesFor(category: String, articles: List<Article>) {
        cache[category] = articles
    }

    fun clearCategoryCache(category: String) {
        cache.remove(category)
    }

    fun clearAll() {
        cache.clear()
    }
}