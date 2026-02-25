package com.example.scanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.data.local.dao.EmployeeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val employeeDao: EmployeeDao
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun onLoginClicked(personalNr: String) {
        viewModelScope.launch {
            if (personalNr.isEmpty()) {
                _loginState.value = LoginState.Error("Personal-Nr. darf nicht leer sein.")
                return@launch
            }
            
            _loginState.value = LoginState.Loading
            
            // Look up the employee using their login number
            val employee = employeeDao.getEmployeeByLoginNumber(personalNr)
            
            if (employee != null) {
                // Login successful, pass the technical employeeId
                _loginState.value = LoginState.Success(employee.employeeId)
            } else {
                _loginState.value = LoginState.Error("Personal-Nr. ($personalNr) nicht gefunden.")
            }
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val employeeId: String) : LoginState()
    data class Error(val message: String) : LoginState()
}