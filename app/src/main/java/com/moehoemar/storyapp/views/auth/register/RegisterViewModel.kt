package com.moehoemar.storyapp.views.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moehoemar.storyapp.data.remote.response.auth.RegisterResponse
import com.moehoemar.storyapp.data.remote.retrofit.auth.AuthApiConfig
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<RegisterResponse>()
    val registerResult: LiveData<RegisterResponse> get() = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = AuthApiConfig.getApiService().register(name, email, password)
                _isLoading.value = false
                if (response.isSuccessful) {
                    _registerResult.value = response.body()
                } else {
                    _error.value = response.message()
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