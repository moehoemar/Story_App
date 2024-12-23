package com.moehoemar.storyapp.views.story

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.databinding.ActivityStoryBinding
import com.moehoemar.storyapp.utils.displayToast
import com.moehoemar.storyapp.utils.wrapEspressoIdlingResource
import com.moehoemar.storyapp.views.MainActivity
import com.moehoemar.storyapp.views.maps.MapsActivity
import com.moehoemar.storyapp.views.story.ui.create.CreateStoryActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStoryBinding
    private lateinit var navController: NavController
    private val preferences: StoryAppPreferences by lazy {
        StoryAppPreferences.getInstance(this.dataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_story) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            val intent = Intent(this, CreateStoryActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            val isLoggedIn = preferences.isLoggedIn().first()
            if (!isLoggedIn) {
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }

            R.id.action_settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }

            R.id.action_map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        val title = getString(R.string.logout)
        val message = getString(R.string.logout_message)
        val builder = AlertDialog.Builder(this@StoryActivity)

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        displayToast(this, getString(R.string.logout_process))
        lifecycleScope.launch {
            wrapEspressoIdlingResource {
                preferences.clearToken()
            }

            val intent = Intent(this@StoryActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}