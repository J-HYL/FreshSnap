package com.marujho.freshsnap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val AGE_KEY = intPreferencesKey("user_age")
        private val GENDER_KEY = stringPreferencesKey("user_gender")
        private val LANGUAGE_KEY = stringPreferencesKey("user_language")

        private val ALLERGIES_KEY = stringSetPreferencesKey("user_allergies")
    }

    //Alergias
    val userAllergies: Flow<Set<String>> = context.dataStore.data
        .map { it[ALLERGIES_KEY] ?: emptySet() }

    suspend fun setUserAllergies(allergies: Set<String>) {
        context.dataStore.edit {
            it[ALLERGIES_KEY] = allergies
        }
    }

    //Dark Mode
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    // Nombre
    val userName: Flow<String> = context.dataStore.data
        .map { it[NAME_KEY] ?: "Usuario" }

    suspend fun setUserName(name: String) {
        context.dataStore.edit {
            it[NAME_KEY] = name
        }
    }

    // Edad
    val userAge: Flow<Int> = context.dataStore.data
        .map { it[AGE_KEY] ?: 0 }

    suspend fun setUserAge(age: Int) {
        context.dataStore.edit {
            it[AGE_KEY] = age
        }
    }

    // Sexo
    val userGender: Flow<String> = context.dataStore.data
        .map { it[GENDER_KEY] ?: "No especificado" }

    suspend fun setUserGender(gender: String) {
        context.dataStore.edit {
            it[GENDER_KEY] = gender
        }
    }

    // Idioma
    val userLanguage: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "Español" }

    suspend fun setUserLanguage(language: String) {
        context.dataStore.edit {
            it[LANGUAGE_KEY] = language
        }
    }
}