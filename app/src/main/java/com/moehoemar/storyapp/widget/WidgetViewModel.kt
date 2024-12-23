package com.moehoemar.storyapp.widget

import androidx.lifecycle.ViewModel
import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem
import com.moehoemar.storyapp.data.remote.retrofit.story.StoryApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetViewModel : ViewModel() {
    suspend fun getStories(token: String): List<ListStoryItem>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = StoryApiConfig.getApiService(token)
                    .getAllStories(page = 1, size = 10, location = 0)
                if (response.isSuccessful) {
                    response.body()?.listStory
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}