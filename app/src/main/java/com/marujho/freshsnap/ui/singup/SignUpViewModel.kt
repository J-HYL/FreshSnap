package com.marujho.freshsnap.ui.singup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    fun register(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            onError("Por favor, rellena todos los campos")
            return
        }

        if (pass != confirmPass) {
            onError("Las contraseñas no coinciden")
            return
        }

        if (pass.length < 6) {
            onError("La contraseña debe tener más 6 caracteres")
            return
        }

        viewModelScope.launch {
            val result = repository.signUp(email, pass, name)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }
}