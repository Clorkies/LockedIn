package edu.citu.csit284.lockedin.caches

import edu.citu.csit284.lockedin.data.Match

object UpcomingMatchesCache {
    private val cache = mutableMapOf<String, List<Match>>()
    fun getMatchesFor(game: String): List<Match>? {
        return cache[game]
    }
    fun storeMatchesFor(game: String, matches: List<Match>) {
        cache[game] = matches
    }
    fun clearGameCache(game: String) {
        cache.remove(game)
    }
    fun clearAll() {
        cache.clear()
    }
}