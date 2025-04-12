package edu.citu.csit284.lockedin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.citu.csit284.lockedin.databinding.ActivityMainBinding
import androidx.navigation.NavDestination
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    private var isNavigatingFromCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        bottomNav = binding.bottomNav
        bottomNav.itemIconTintList = null

        updateSelectedIcon(bottomNav.menu, bottomNav.selectedItemId, true)

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            val currentItemId = bottomNav.selectedItemId

            if (currentItemId != item.itemId) {
                updateSelectedIcon(bottomNav.menu, currentItemId, false)
                updateSelectedIcon(bottomNav.menu, item.itemId, true)

                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()

                isNavigatingFromCode = true
                try {
                    navController.navigate(item.itemId, null, navOptions)
                } catch (_: IllegalArgumentException) {}
            }

            true
        }

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            if (!isNavigatingFromCode) {
                val item = bottomNav.menu.findItem(destination.id)
                if (item != null && !item.isChecked) {
                    bottomNav.menu.setGroupCheckable(0, true, true)
                    item.isChecked = true
                }
            }

            for (i in 0 until bottomNav.menu.size()) {
                val menuItem = bottomNav.menu.getItem(i)
                val isSelected = menuItem.itemId == destination.id
                updateSelectedIcon(bottomNav.menu, menuItem.itemId, isSelected)
            }

            isNavigatingFromCode = false
        }
    }

    private fun updateSelectedIcon(menu: Menu, itemId: Int, isSelected: Boolean) {
        val item = menu.findItem(itemId)
        item?.let {
            when (item.itemId) {
                R.id.landingFragment -> item.setIcon(if (isSelected) R.drawable.btn_home_highlighted else R.drawable.btn_home)
                R.id.gamesFragment -> item.setIcon(if (isSelected) R.drawable.btn_games_highlighted else R.drawable.btn_games)
                R.id.liveFragment -> item.setIcon(if (isSelected) R.drawable.btn_live_highlighted else R.drawable.btn_live)
                R.id.exploreFragment -> item.setIcon(if (isSelected) R.drawable.btn_explore_highlighted else R.drawable.btn_explore)
                else -> {}
            }
        }
    }
}

