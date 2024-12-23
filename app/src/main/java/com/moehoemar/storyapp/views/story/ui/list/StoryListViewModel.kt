package com.moehoemar.storyapp.views.story.ui.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.moehoemar.storyapp.data.StoryRepository
import com.moehoemar.storyapp.data.di.Injection
import com.moehoemar.storyapp.data.remote.response.story.ListStoryItem

class StoryListViewModel(storyRepository: StoryRepository) : ViewModel() {

    val stories: LiveData<PagingData<ListStoryItem>> =
        storyRepository.getAllStories().cachedIn(viewModelScope)
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryListViewModel(Injection.provideStoryRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

