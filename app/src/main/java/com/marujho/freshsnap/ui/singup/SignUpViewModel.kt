package com.marujho.freshsnap.ui.singup

import android.util.Log
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
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            onError("Por favor, rellena todos los campos")
            return
        }

        if (pass != confirmPass) {
            onError("Las contraseñas no coinciden")
            return
        }

        if (pass.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            Log.d("SIGNUP_TEST", "Intentando registrar: $email")

            val result = repository.signUp(email, pass, name)

            if (result.isSuccess) {
                Log.d("SIGNUP_TEST", "bien")
                onSuccess()
            } else {
                val errorException = result.exceptionOrNull()
                val errorMsg = errorException?.message ?: "Error "

                Log.e("SIGNUP_TEST", "Fallo en el registro", errorException)

                onError(errorMsg)
            }
        }
    }
}