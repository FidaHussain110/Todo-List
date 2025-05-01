package uk.ac.tees.mad.E4294395

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreferences {
    private val THEME_KEY = stringPreferencesKey("theme")

    fun getTheme(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: "Light"
        }
    }

    suspend fun setTheme(context: Context, theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}