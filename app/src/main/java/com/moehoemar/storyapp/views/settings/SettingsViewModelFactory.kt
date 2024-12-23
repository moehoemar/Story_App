package com.moehoemar.storyapp.views.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences

class SettingsViewModelFactory(private val preferences: StoryAppPreferences) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        @Volatile
        private var instance: SettingsViewModelFactory? = null

        fun getInstance(preferences: StoryAppPreferences) =
            instance ?: synchronized(this) {
                instance ?: SettingsViewModelFactory(preferences).also { instance = it }
            }
    }
}
