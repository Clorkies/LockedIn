package edu.citu.csit284.lockedin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import edu.citu.csit284.lockedin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = binding.bottomNav
        bottomNav.itemIconTintList = null

        updateSelectedIcon(bottomNav.menu, bottomNav.selectedItemId, true)

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            val previousItemId = bottomNav.selectedItemId

            if (previousItemId != item.itemId) {
                updateSelectedIcon(bottomNav.menu, previousItemId, false)
                updateSelectedIcon(bottomNav.menu, item.itemId, true)
                navController.navigate(item.itemId)
            }

            true
        }
    }


    private fun updateSelectedIcon(menu: Menu, itemId: Int, isSelected: Boolean) {
        val item = menu.findItem(itemId)
        if (item != null) {
            when (item.itemId) {
                R.id.landingFragment -> item.setIcon(if (isSelected) R.drawable.btn_home_highlighted else R.drawable.btn_home)
                R.id.gamesFragment -> item.setIcon(if (isSelected) R.drawable.btn_games_highlighted else R.drawable.btn_games)
                R.id.liveFragment -> item.setIcon(if (isSelected) R.drawable.btn_live_highlighted else R.drawable.btn_live)
                R.id.exploreFragment -> item.setIcon(if (isSelected) R.drawable.btn_explore_highlighted else R.drawable.btn_explore)
            }
        }
    }
}
