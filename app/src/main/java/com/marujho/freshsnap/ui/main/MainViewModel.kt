package com.marujho.freshsnap.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var userName by mutableStateOf("Cargando...")
        private set

    init {
        fetchUserName()
    }

    private fun fetchUserName() {
        viewModelScope.launch {
            val result = repository.getUserName()
            userName = result.getOrDefault("Usuario")
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        repository.logout()
        onLogoutSuccess()
    }
}