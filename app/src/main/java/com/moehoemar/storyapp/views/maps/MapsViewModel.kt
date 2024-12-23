package com.moehoemar.storyapp.views.maps

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moehoemar.storyapp.data.di.Injection
import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem
import com.moehoemar.storyapp.data.remote.retrofit.story.StoryApiConfig
import kotlinx.coroutines.launch

class MapsViewModel(private val token: String) : ViewModel(){
    private val _stories = MutableLiveData<List<ListStoryItem>?>()
    val stories: LiveData<List<ListStoryItem>?> get() = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun getStoriestWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = StoryApiConfig.getApiService(token).getStoriesWithLocation()
                if (response.isSuccessful) {
                    _isLoading.value = false
                    _stories.value = response.body()?.listStory
                } else {
                    _isLoading.value = false
                    _error.value = "Failed to get stories: ${response.message()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }
}

class MapsViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            val token = Injection.provideUserToken(context)
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}