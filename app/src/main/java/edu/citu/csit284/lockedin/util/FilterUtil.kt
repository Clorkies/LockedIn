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

    fun improvedFilterArticlesByGame(articles: List<Article>, gameName: String): List<Article> {
        if (gameName.isEmpty()) return articles

        val gameKeywords = getGameKeywords(gameName.lowercase())
        val esportsKeywords = listOf(
            "tournament", "championship", "esports", "competitive",
            "pro player", "team", "match", "gameplay", "patch", "update",
            "meta", "pro scene", "league", "rank", "ranked", "professional"
        )

        val scoredArticles = articles.map { article ->
            val title = article.title?.lowercase() ?: ""
            val description = article.description?.lowercase() ?: ""

            var score = 0

            gameKeywords.forEach { keyword ->
                if (title.contains(keyword.lowercase())) {
                    score += 10
                }
            }

            esportsKeywords.forEach { keyword ->
                if (title.contains(keyword.lowercase())) {
                    score += 5
                }
            }

            gameKeywords.forEach { keyword ->
                if (description.contains(keyword.lowercase())) {
                    score += 3
                }
            }

            esportsKeywords.forEach { keyword ->
                if (description.contains(keyword.lowercase())) {
                    score += 2
                }
            }

            val nonEnglishIndicators = listOf("ä", "ö", "ü", "é", "è", "ê", "à", "á", "â", "ñ", "ç")
            val hasMultipleNonEnglishChars = nonEnglishIndicators.count { title.contains(it) } > 2

            if (hasMultipleNonEnglishChars) {
                score -= 50
            }

            Pair(article, score)
        }

        return scoredArticles
            .filter { (_, score) -> score >= 10 }
            .sortedByDescending { (_, score) -> score }
            .map { (article, _) -> article }
    }

    fun getGameKeywords(gameName: String): List<String> {
        return when (gameName) {
            "valorant" -> listOf(
                "valorant", "riot games", "vct", "valorant champions",
                "valorant esports",  "radiant", "valorant patch", "valorant update",
                "valorant skin", "valorant pro", "tenz", "aspas", "boaster", "loud", "fnatic", "sentinels"
            )

            "leagueee" -> listOf(
                "league of legends", "lol", "lec", "lcs", "lck", "lpl", "msi", "worlds", "world championship",
                "riot games", "faker", "caps", "rekkles", "uzi", "arteezy", "t1", "g2", "fnatic", "summoner's rift",
                "champ select", "rift", "lol patch", "lol meta", "baron", "drake", "nexus", "inhibitor"
            )

            "league" -> listOf(
                "league of legends", "lol",
                "riot games", "faker", "summoner's rift",
                "champ select", "baron", "nexus", "wild rift"
            )

            "csgo" -> listOf(
                "counter-strike", "csgo", "cs:go", "counter-strike 2", "valve",
                "pgl", "faceit", "hltv", "navi", "astralis", "vitality", "simple", "zywoo", "bomb plant",
                "dust2", "mirage", "inferno"
            )

            "dota" -> listOf(
                "dota", "dota 2", "valve", "ti", "the international", "dpc", "dota pro circuit", "esl one",
                "dreamleague", "arlington major", "gpk", "miracle", "n0tail", "og", "team spirit", "lgd",
                "dire", "radiant", "ancient", "roshan", "ranked mmr", "pubs", "meta heroes"
            )

            "mlbb" -> listOf(
                "mlbb", "mobile legends", "moonton", "ml patch", "ml update",
                "ranked", "mpl", "mythic", "legend",
                "game meta", "turret", "lord", "laning", "jungle", "moba"
            )

            "overwatch" -> listOf(
                "overwatch", "overwatch 2", "owl", "overwatch league", "blizzard", "blizzard entertainment",
                "tracer", "genji", "hanzo", "tank", "dps", "support", "payload", "push", "ranked",
                "competitive", "watchpoint", "hero rework", "ultimates", "goats comp", "contenders", "esports"
            )

            else -> listOf(gameName)
        }
    }

    fun getGameNameById(id: Int): String {
        return when (id) {
            1 -> "valorant"
            2 -> "league"
            3 -> "csgo"
            4 -> "dota"
            5 -> "mlbb"
            6 -> "overwatch"
            else -> ""
        }
    }
}