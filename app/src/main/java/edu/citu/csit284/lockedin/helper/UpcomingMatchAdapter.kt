package edu.citu.csit284.lockedin.helper

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.activities.MatchDetailsActivity
import edu.citu.csit284.lockedin.data.Match
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UpcomingMatchAdapter(private val listOfMatches : List<Match>):
    RecyclerView.Adapter<UpcomingMatchAdapter.ItemViewHolder>(){
    companion object {
        const val EXTRA_MATCH = "extra_match"
    }
    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val tvLeagueName = view.findViewById<TextView>(R.id.tv_league_name)
        val tvSerieName = view.findViewById<TextView>(R.id.tv_serie_name)
        val tvTournamentName = view.findViewById<TextView>(R.id.tv_tournament_name)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvTime = view.findViewById<TextView>(R.id.tv_time)
        val ivTeam1Logo = view.findViewById<ImageView>(R.id.iv_team1_logo)
        val tvTeam1Name = view.findViewById<TextView>(R.id.tv_team1_name)
        val ivTeam2Logo = view.findViewById<ImageView>(R.id.iv_team2_logo)
        val tvTeam2Name = view.findViewById<TextView>(R.id.tv_team2_name)
        val tvMatchType = view.findViewById<TextView>(R.id.tv_match_type)
        val tvStreamLink = view.findViewById<TextView>(R.id.tv_stream_link)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingMatchAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_match, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpcomingMatchAdapter.ItemViewHolder, position: Int) {
        val match = listOfMatches[position]
        val context = holder.itemView.context

        holder.tvLeagueName.text = match.league.name
        holder.tvSerieName.text = match.serie.full_name
        holder.tvTournamentName.text = match.tournament.name
        val utcDateString = "${getCurrentYear()}/${match.date}"
        val utcTimeString = match.time

        val utcDateTimeString = "$utcDateString $utcTimeString"

        val utcFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val utcDate = utcFormat.parse(utcDateTimeString)

            val phtDateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
            phtDateFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
            val philippineDateString = phtDateFormat.format(utcDate)
            holder.tvDate.text = philippineDateString

            val phtTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            phtTimeFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
            val philippineTimeString = phtTimeFormat.format(utcDate)
            holder.tvTime.text = philippineTimeString

        } catch (e: Exception) {
            holder.tvDate.text = "Error"
            holder.tvTime.text = "Error"
            e.printStackTrace()
        }

        if (match.opponents.size >= 2) {
            val team1 = match.opponents[0].opponent
            val team2 = match.opponents[1].opponent

            holder.tvTeam1Name.text = team1.name
            holder.tvTeam2Name.text = team2.name

            Glide.with(holder.itemView.context)
                .load(team1.image_url)
                .placeholder(R.drawable.default_pfp)
                .error(R.drawable.default_pfp)
                .into(holder.ivTeam1Logo)

            Glide.with(holder.itemView.context)
                .load(team2.image_url)
                .placeholder(R.drawable.default_pfp)
                .error(R.drawable.default_pfp)
                .into(holder.ivTeam2Logo)
        } else {
            holder.tvTeam1Name.text = match.opponents.getOrNull(0)?.opponent?.name ?: "TBD"
            holder.tvTeam2Name.text = match.opponents.getOrNull(1)?.opponent?.name ?:"TBD"

            holder.ivTeam1Logo.setImageResource(R.drawable.default_pfp)
            holder.ivTeam2Logo.setImageResource(R.drawable.red_pfp)
        }

        holder.tvMatchType.text = "BO${match.number_of_games}"

        if (match.streams_list.isNotEmpty()) {
            holder.tvStreamLink.text = match.streams_list[0].raw_url
        } else {
            holder.tvStreamLink.text = "No stream available"
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MatchDetailsActivity::class.java)
            val gson = Gson()
            val matchJson = gson.toJson(match)
            intent.putExtra(LiveMatchAdapter.EXTRA_MATCH, matchJson)
            context.startActivity(intent)
        }
    }

    private fun getCurrentYear(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }
    override fun getItemCount(): Int = listOfMatches.size

}