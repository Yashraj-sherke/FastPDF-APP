package com.fastpdf.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed theme preferences.
 * Persists dark mode toggle across app restarts.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreferences {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

    /**
     * Observe dark mode setting as a Flow.
     */
    fun isDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY] ?: false
        }
    }

    /**
     * Toggle dark mode on/off.
     */
    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    /**
     * Check if onboarding has been completed.
     * Returns true if this is the first launch (onboarding NOT completed yet).
     */
    fun isFirstLaunch(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            !(prefs[ONBOARDING_COMPLETED_KEY] ?: false)
        }
    }

    /**
     * Mark onboarding as completed.
     */
    suspend fun setOnboardingComplete(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = true
        }
    }
}

