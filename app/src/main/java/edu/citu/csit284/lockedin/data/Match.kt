package edu.citu.csit284.lockedin.data

data class Match(
    val id: Long,
    val league: League,
    val serie: Serie,
    val tournament: Tournament,
    val videogame : Videogame,
    val scheduled_at: String,
    val opponents: List<Opponent>,
    val results: List<Result>,
    val streams_list: List<Stream>,
    val match_type: String,
    val number_of_games: Int
) {
    data class League(
        val id: Long,
        val name: String,
        val image_url: String?,
        val videogame : String?
    )
    data class Videogame(
        val name : String
    )

    data class Serie(
        val id: Long,
        val full_name: String,
        val slug: String
    )

    data class Tournament(
        val id: Long,
        val name: String
    )

    data class Opponent(
        val opponent: OpponentDetails
    )

    data class OpponentDetails(
        val id: Long,
        val name: String,
        val image_url: String?
    )

    data class Result(
        val teamId: Int,
        val score: Int
    )

    data class Stream(
        val raw_url: String
    )

    val date: String
        get() = scheduled_at.split("T")[0].substring(5).replace("-", "/")


    val time: String
        get() = scheduled_at.split("T").getOrNull(1)?.substring(0, 5) ?: ""

    val streamlink: String
        get() = streams_list.firstOrNull()?.raw_url ?: ""
}