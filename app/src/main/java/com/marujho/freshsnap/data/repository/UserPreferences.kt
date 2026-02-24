package com.marujho.freshsnap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val AGE_KEY = intPreferencesKey("user_age")
        private val GENDER_KEY = stringPreferencesKey("user_gender")
        private val LANGUAGE_KEY = stringPreferencesKey("user_language")
        private val ALLERGIES_KEY = stringSetPreferencesKey("user_allergies")
        private val EXPIRY_ALERT_DAYS_KEY = intPreferencesKey("expiry_alert_days")
        private val EXPIRY_RED_DAYS_KEY = intPreferencesKey("expiry_red_days")
        private val EXPIRY_YELLOW_DAYS_KEY = intPreferencesKey("expiry_yellow_days")
    }

    // Caducidad preferencias
    val expiryRedDays: Flow<Int> = context.dataStore.data
        .map { it[EXPIRY_RED_DAYS_KEY] ?: 2 }

    suspend fun setExpiryRedDays(days: Int) {
        context.dataStore.edit { it[EXPIRY_RED_DAYS_KEY] = days }
    }

    val expiryYellowDays: Flow<Int> = context.dataStore.data
        .map { it[EXPIRY_YELLOW_DAYS_KEY] ?: 5 }

    suspend fun setExpiryYellowDays(days: Int) {
        context.dataStore.edit { it[EXPIRY_YELLOW_DAYS_KEY] = days }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // Alertas de caducidad
    val expiryAlertDays: Flow<Int> = context.dataStore.data
        .map { it[EXPIRY_ALERT_DAYS_KEY] ?: 3 }

    suspend fun setExpiryAlertDays(days: Int) {
        context.dataStore.edit { it[EXPIRY_ALERT_DAYS_KEY] = days }
    }

    // Alergias
    val userAllergies: Flow<Set<String>> = context.dataStore.data
        .map { it[ALLERGIES_KEY] ?: emptySet() }

    suspend fun setUserAllergies(allergies: Set<String>) {
        context.dataStore.edit { it[ALLERGIES_KEY] = allergies }
    }

    // Dark Mode
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    // Nombre
    val userName: Flow<String> = context.dataStore.data
        .map { it[NAME_KEY] ?: "Usuario" }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[NAME_KEY] = name }
    }

    // Edad
    val userAge: Flow<Int> = context.dataStore.data
        .map { it[AGE_KEY] ?: 0 }

    suspend fun setUserAge(age: Int) {
        context.dataStore.edit { it[AGE_KEY] = age }
    }

    // Sexo
    val userGender: Flow<String> = context.dataStore.data
        .map { it[GENDER_KEY] ?: "No especificado" }

    suspend fun setUserGender(gender: String) {
        context.dataStore.edit { it[GENDER_KEY] = gender }
    }

    // Idioma
    val userLanguage: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "Sistema" }

    suspend fun setUserLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }
}