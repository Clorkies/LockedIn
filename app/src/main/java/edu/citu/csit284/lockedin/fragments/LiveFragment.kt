package edu.citu.csit284.lockedin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import edu.citu.csit284.lockedin.ProfileActivity
import edu.citu.csit284.lockedin.R
import edu.citu.csit284.lockedin.TournamentActivity
import edu.citu.csit284.lockedin.data.Tournament
import edu.citu.csit284.lockedin.helper.TournamentCustomListView

class LiveFragment : Fragment() {

    private var caller: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caller = arguments?.getString("caller")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProfile = view.findViewById<ImageButton>(R.id.button_profile)
        btnProfile.setOnClickListener {startActivity(Intent(requireContext(), ProfileActivity::class.java)) }

        val btnBack = view.findViewById<ImageButton>(R.id.button_back)
        btnBack.setOnClickListener {
            when (caller) {
                "landing" -> findNavController().navigate(R.id.landingFragment)
                "game" -> findNavController().navigate(R.id.gamesFragment)
                "live" -> findNavController().navigate(R.id.liveFragment)
                "explore" -> {}
                else -> requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }




        val lvTournaments = view.findViewById<ListView>(R.id.lvTournaments)

        val tournaments = listOf(
            Tournament(R.drawable.tourney_sample1, "Title 1", getString(R.string.lorem), "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample2, "Title 2", getString(R.string.lorem), "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample3, "Title 3", getString(R.string.lorem), "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample1, "Title 4", getString(R.string.lorem), "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample2, "Title 5", getString(R.string.lorem), "2025-xx-xx"),
            Tournament(R.drawable.tourney_sample3, "Title 6", getString(R.string.lorem), "2025-xx-xx")
        )

        lvTournaments.adapter = TournamentCustomListView(
            requireContext(),
            tournaments,
            onClick = {
                val intent = Intent(requireContext(), TournamentActivity::class.java).apply {
                    putExtra("imageResource", it.imgResId)
                    putExtra("title", it.name)
                    putExtra("articleText", it.description)
                    putExtra("caller", "live")
                }
                startActivity(intent)
            }
        )
    }

}

