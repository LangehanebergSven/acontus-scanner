package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun onLoginClicked(personalNr: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            // Here you would typically validate the personalNr against a repository
            // For now, we'll just simulate a network call and assume success if not empty
            kotlinx.coroutines.delay(1000)
            if (personalNr.isNotEmpty()) {
                // In a real app, you'd get the employee data from the repository
                // and probably save the session
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Personal-Nr. darf nicht leer sein.")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
