package edu.citu.csit284.lockedin.util

import edu.citu.csit284.lockedin.data.Article

object FilterUtil {
    fun filterArticlesByGame(articles: List<Article>, gameName: String): List<Article> {
        if (gameName.isEmpty()) return articles

        val gameKeywords = getGameKeywords(gameName.lowercase())

        return articles.filter { article ->
            val titleMatches = article.title?.lowercase()?.let { title ->
                gameKeywords.any { keyword -> title.contains(keyword) }
            } ?: false

            val descriptionMatches = article.description?.lowercase()?.let { description ->
                gameKeywords.any { keyword -> description.contains(keyword) }
            } ?: false


            titleMatches || descriptionMatches
        }
    }

    private fun getGameKeywords(gameName: String): List<String> {
        return when (gameName) {
            "valorant" -> listOf("valorant", "riot games", "vct", "champions tour")
            "league" -> listOf("league of legends", "lol", "lec", "lcs", "worlds", "msi", "riot games")
            "csgo" -> listOf("counter-strike", "cs2", "csgo", "cs:go", "cs2", "esl", "blast", "valve")
            "dota" -> listOf("dota", "dota 2", "ti", "the international", "valve")
            "rivals" -> listOf("rivals", "mobile legends", "ml", "mlbb")
            "overwatch" -> listOf("overwatch", "owl", "overwatch league", "blizzard")
            else -> listOf(gameName)
        }
    }

    fun getGameNameById(id: Int): String {
        return when (id) {
            1 -> "valorant"
            2 -> "league"
            3 -> "csgo"
            4 -> "dota"
            5 -> "rivals"
            6 -> "overwatch"
            else -> ""
        }
    }
}