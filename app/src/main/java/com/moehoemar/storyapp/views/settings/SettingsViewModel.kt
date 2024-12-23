package com.moehoemar.storyapp.views.settings

import androidx.lifecycle.ViewModel
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences

class SettingsViewModel(private val preferences: StoryAppPreferences) : ViewModel() {
    fun getLanguage() = preferences.getLanguage()
    suspend fun setLanguage(language: String) = preferences.setLanguage(language)

    fun getIsDarkModeEnabled() = preferences.getIsDarkModeEnabled()
    suspend fun setIsDarkModeEnabled(isEnabled: Boolean) = preferences.setIsDarkModeEnabled(isEnabled)
}