package com.moehoemar.storyapp.views.story.ui.detail

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

class DetailStoryViewModel(
    private val token: String,
    private val storyId: String
) : ViewModel() {
    private val _story = MutableLiveData<ListStoryItem?>()
    val story: LiveData<ListStoryItem?> get() = _story

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        getStoryById()
    }

    private fun getStoryById() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val apiService = StoryApiConfig.getApiService(token)
                val response = apiService.getStoryById(storyId)
                if (response.isSuccessful) {
                    _story.value = response.body()?.story
                } else {
                    _error.value = "Failed to get story: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class DetailStoryViewModelFactory(
    private val context: Context,
    private val storyId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailStoryViewModel::class.java)) {
            val token = Injection.provideUserToken(context)
            @Suppress("UNCHECKED_CAST")
            return DetailStoryViewModel(token, storyId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}