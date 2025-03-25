package edu.citu.csit284.lockedin.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.data.Tournament

class TournamentCustomListView(
    private val context: Context,
    private val tournamentList: List<Tournament>,
    private val onClick: (Tournament) -> Unit
): BaseAdapter() {
    override fun getCount(): Int = tournamentList.size
    override fun getItem(pos: Int): Any = tournamentList[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.tournament_item,
            parent,
            false
        )

        val img = view.findViewById<ImageView>(R.id.imgHeader)
        val name = view.findViewById<TextView>(R.id.tvName)
        val desc = view.findViewById<TextView>(R.id.tvDesc)

        val tourney = tournamentList[pos]

        img.setImageResource(tourney.imgResId)
        name.setText(tourney.name)
        desc.setText(tourney.description)

        view.setOnClickListener {
            onClick(tourney)
        }

        return view
    }

}