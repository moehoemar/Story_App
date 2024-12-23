package com.moehoemar.storyapp.views.story.ui.create

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moehoemar.storyapp.data.di.Injection
import com.moehoemar.storyapp.data.remote.response.story.AddNewStoryResponse
import com.moehoemar.storyapp.data.remote.retrofit.story.StoryApiConfig
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CreateStoryViewModel(private val token: String) : ViewModel() {
    private val _createStoryResponse = MutableLiveData<AddNewStoryResponse>()
    val createStoryResponse: MutableLiveData<AddNewStoryResponse> get() = _createStoryResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    fun createNewStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody,
        long: RequestBody,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response =
                    StoryApiConfig.getApiService(token)
                        .createNewStory(description, photo, lat, long)
                if (response.isSuccessful) {
                    _isLoading.value = false
                    _createStoryResponse.value = response.body()
                } else {
                    _isLoading.value = false
                    _error.value = "Failed to create story: ${response.message()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
            }
        }
    }

    fun resetError() {
        _error.value = null
    }
}

class CreateStoryViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoryViewModel::class.java)) {
            val token = Injection.provideUserToken(context)
            @Suppress("UNCHECKED_CAST")
            return CreateStoryViewModel(token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}