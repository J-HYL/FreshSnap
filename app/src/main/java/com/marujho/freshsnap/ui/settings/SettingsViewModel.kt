package com.marujho.freshsnap.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

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
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {

            FirebaseAuth.getInstance().signOut()

            //userPreferences.clearAll()

            onLoggedOut()
        }
    }

    //Para los checkboxes de los dias de caducidad
    val expiryAlertDays = userPreferences.expiryAlertDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    fun setExpiryDays(days: Int) {
        viewModelScope.launch {
            userPreferences.setExpiryAlertDays(days)
        }
    }

}