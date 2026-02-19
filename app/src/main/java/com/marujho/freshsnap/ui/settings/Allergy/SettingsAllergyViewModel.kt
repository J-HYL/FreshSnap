package com.marujho.freshsnap.ui.settings.Allergy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsAllergyViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val userAllergies = userPreferences.userAllergies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun saveAllergies(allergies: Set<String>) {
        viewModelScope.launch {
            userPreferences.setUserAllergies(allergies)
        }
    }
}
