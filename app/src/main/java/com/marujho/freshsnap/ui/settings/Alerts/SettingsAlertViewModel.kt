package com.marujho.freshsnap.ui.settings.Alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsAlertViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val redDays = userPreferences.expiryRedDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val yellowDays = userPreferences.expiryYellowDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    fun updateRedDays(days: Int) {
        viewModelScope.launch {
            userPreferences.setExpiryRedDays(days)

            val currentYellow = yellowDays.value
            if (currentYellow <= days) {
                userPreferences.setExpiryYellowDays(days + 1)
            }
        }
    }

    fun updateYellowDays(days: Int) {
        viewModelScope.launch {
            val currentRed = redDays.value

            if (days >= currentRed) {
                userPreferences.setExpiryYellowDays(days)
            }
        }
    }
}