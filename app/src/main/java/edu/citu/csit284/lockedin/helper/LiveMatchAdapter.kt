package edu.citu.csit284.lockedin.helper

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Match

class LiveMatchAdapter(private val listOfMatches : List<Match>):
    RecyclerView.Adapter<LiveMatchAdapter.ItemViewHolder>(){
    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val tvLeagueName = view.findViewById<TextView>(R.id.tv_league_name)
        val tvSerieName = view.findViewById<TextView>(R.id.tv_serie_name)
        val ivTeam1Logo = view.findViewById<ImageView>(R.id.iv_team1_logo)
        val tvTeam1Name = view.findViewById<TextView>(R.id.tv_team1_name)
        val ivTeam2Logo = view.findViewById<ImageView>(R.id.iv_team2_logo)
        val tvTeam1Score = view.findViewById<TextView>(R.id.tv_team1_score)
        val tvTeam2Score = view.findViewById<TextView>(R.id.tv_team2_score)
        val tvTeam2Name = view.findViewById<TextView>(R.id.tv_team2_name)
        val tvMatchType = view.findViewById<TextView>(R.id.tv_match_type)
        val tvStreamLink = view.findViewById<TextView>(R.id.tv_stream_link)
        val btnWatch = view.findViewById<Button>(R.id.overlayButton)
        val tvGame = view.findViewById<TextView>(R.id.tv_game)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMatchAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_live_match, parent, false)
        return ItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: LiveMatchAdapter.ItemViewHolder, position: Int) {
        val match = listOfMatches[position]

        holder.tvLeagueName.text = match.league.name
        holder.tvSerieName.text = match.serie.full_name

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
        if(match.results.isNotEmpty()){
            holder.tvTeam1Score.text = match.results[0].score.toString()
            holder.tvTeam2Score.text = match.results[1].score.toString()
        }
        holder.tvMatchType.text = "Best of ${match.number_of_games}"

        holder.tvGame.text = match.videogame.name

        if (match.streams_list.isNotEmpty()) {
            holder.tvStreamLink.text = match.streams_list[0].raw_url
        } else {
            holder.tvStreamLink.text = "No stream available"
        }
        holder.btnWatch.setOnClickListener {
            if(holder.tvStreamLink.text != "No stream available"){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(holder.tvStreamLink.text.toString()))
                holder.btnWatch.context.startActivity(intent)
            }else{
                Toast.makeText(holder.btnWatch.context,"No stream avaialble", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun getItemCount(): Int = listOfMatches.size

}