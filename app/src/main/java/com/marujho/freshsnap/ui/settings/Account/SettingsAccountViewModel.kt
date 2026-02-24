package com.marujho.freshsnap.ui.settings.Account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsAccountViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val userName = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userAge = userPreferences.userAge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userGender = userPreferences.userGender
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userLanguage = userPreferences.userLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Español")

    fun saveUserData(name: String, age: Int, gender: String, language: String) {
        viewModelScope.launch {
            userPreferences.setUserName(name)
            userPreferences.setUserAge(age)
            userPreferences.setUserGender(gender)
            userPreferences.setUserLanguage(language)
        }
    }
}
