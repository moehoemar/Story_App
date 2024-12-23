package com.moehoemar.storyapp.data.di

import android.content.Context
import com.moehoemar.storyapp.data.StoryRepository
import com.moehoemar.storyapp.data.local.room.StoryDatabase
import com.moehoemar.storyapp.data.preferences.StoryAppPreferences
import com.moehoemar.storyapp.data.preferences.dataStore
import com.moehoemar.storyapp.data.remote.retrofit.story.StoryApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val userToken = provideUserToken(context)
        val storyApiService = StoryApiConfig.getApiService(userToken)
        val database = StoryDatabase.getInstance(context)
        return StoryRepository(database, storyApiService)
    }
    fun providePreferences(context: Context): StoryAppPreferences {
        return StoryAppPreferences.getInstance(context.dataStore)
    }
    fun provideUserToken(context: Context): String {
        val preferences = StoryAppPreferences.getInstance(context.dataStore)
        val userToken: String = runBlocking { preferences.getToken().first() }
        return userToken
    }
}