package com.moehoemar.storyapp.views

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.databinding.ActivityMainBinding
import com.moehoemar.storyapp.views.story.StoryActivity
import com.moehoemar.storyapp.views.story.adapter.StoryListAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var adapter: StoryListAdapter
    private val preferences: StoryAppPreferences by lazy {
        StoryAppPreferences.getInstance(this.dataStore)
    }

    override fun attachBaseContext(newBase: Context) {
        val preferences = StoryAppPreferences.getInstance(newBase.dataStore)
        val languageCode = runBlocking { preferences.getLanguage().first() }
        val locale = Locale(languageCode)
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateConfiguration()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        applyDarkModeSetting()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        checkLoginStatus(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if(::adapter.isInitialized) {
            adapter.refresh()
        }
   }

    private fun updateConfiguration() {
        val languageCode = runBlocking { preferences.getLanguage().first() }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        val newConfig = Configuration(resources.configuration)
        newConfig.setLocale(locale)
        baseContext.createConfigurationContext(newConfig)
    }

    private fun applyDarkModeSetting() {
        lifecycleScope.launch {
            val isDarkMode = preferences.getIsDarkModeEnabled().first()
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun checkLoginStatus(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                val isLoggedIn = preferences.isLoggedIn().first()
                if (isLoggedIn) {
                    startActivity(Intent(this@MainActivity, StoryActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        fun restart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}