package com.marujho.freshsnap.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val isDarkMode = userPreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }
}