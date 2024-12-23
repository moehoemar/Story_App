package com.moehoemar.storyapp.views.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.databinding.FragmentSettingsBinding
import com.moehoemar.storyapp.utils.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by lazy {
        val preferences = StoryAppPreferences.getInstance(requireContext().dataStore)
        SettingsViewModelFactory.getInstance(preferences).create(SettingsViewModel::class.java)
    }

    private var isFirstSelection = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.settings)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        setupSpinner()
        loadCurrentSettings()
    }

    private fun setupSpinner() {
        val languages = arrayOf("English", "Indonesia")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }


                val selectedLanguage = when (position) {
                    1 -> "in"
                    else -> "en"
                }

                lifecycleScope.launch {
                    val currentLanguage = settingsViewModel.getLanguage().first()
                    if (selectedLanguage != currentLanguage) {
                        showLanguageChangeDialog(selectedLanguage)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            val currentLanguage = settingsViewModel.getLanguage().first()
            val position = when (currentLanguage) {
                "in" -> 1
                else -> 0
            }
            binding.spinnerLanguage.setSelection(position)
        }

        lifecycleScope.launch {
            val isDarkMode = settingsViewModel.getIsDarkModeEnabled().first()
            binding.switchDarkMode.isChecked = isDarkMode
            setupDarkModeSwitch()
        }
    }

    private fun showLanguageChangeDialog(newLanguage: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Change Language")
            .setMessage("Do you want to change the language? The app will restart.")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    settingsViewModel.setLanguage(newLanguage)
                    applyLanguageAndRestart(newLanguage)
                }
            }
            .setNegativeButton("No") { _, _ ->
                lifecycleScope.launch {
                    val currentLanguage = settingsViewModel.getLanguage().first()
                    val position = when (currentLanguage) {
                        "in" -> 1
                        else -> 0
                    }
                    binding.spinnerLanguage.setSelection(position)
                }
            }
            .show()
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsViewModel.setIsDarkModeEnabled(isChecked)
                updateTheme(isChecked)
            }
        }
    }

    private fun updateTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun applyLanguageAndRestart(languageCode: String) {
        val context = LocaleHelper.setLocale(requireContext(), languageCode)
        requireActivity().resources.updateConfiguration(
            context.resources.configuration,
            context.resources.displayMetrics
        )

        val intent = requireActivity().packageManager
            .getLaunchIntentForPackage(requireActivity().packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        intent?.let { startActivity(it) }
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}