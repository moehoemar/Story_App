package com.moehoemar.storyapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "storyapp_preferences")

@Suppress("PrivatePropertyName")
class StoryAppPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val NAME_KEY = stringPreferencesKey("name")
    private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val IS_DARK_MODE_ENABLED_KEY = booleanPreferencesKey("is_dark")

    suspend fun saveToken(token: String, name: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[NAME_KEY] = name
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(NAME_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    fun getToken(): Flow<String> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] ?: ""
    }

    fun getName(): Flow<String> = dataStore.data.map { preferences ->
        preferences[NAME_KEY] ?: ""
    }

    fun getLanguage(): Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    fun getIsDarkModeEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_ENABLED_KEY] ?: false
    }

    suspend fun setIsDarkModeEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_ENABLED_KEY] = isEnabled
        }
    }

    fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryAppPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): StoryAppPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = StoryAppPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}