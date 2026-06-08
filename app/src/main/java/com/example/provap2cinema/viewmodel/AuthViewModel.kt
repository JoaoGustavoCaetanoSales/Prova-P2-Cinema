package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.User
import com.example.provap2cinema.model.UserType
import com.example.provap2cinema.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            authState = AuthState.Loading
            repository.fetchCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        authState = AuthState.Success(user)
                    } else {
                        authState = AuthState.Idle
                    }
                }
                .onFailure {
                    authState = AuthState.Idle
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authState = AuthState.Loading
            repository.signIn(email, password)
                .onSuccess { user ->
                    authState = AuthState.Success(user)
                }
                .onFailure { error ->
                    authState = AuthState.Error(error.message ?: "Erro desconhecido")
                }
        }
    }

    fun signUp(name: String, email: String, password: String, type: UserType) {
        viewModelScope.launch {
            authState = AuthState.Loading
            repository.signUp(name, email, password, type)
                .onSuccess { user ->
                    authState = AuthState.Success(user)
                }
                .onFailure { error ->
                    authState = AuthState.Error(error.message ?: "Erro desconhecido")
                }
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }
    
    fun signOut() {
        repository.signOut()
        authState = AuthState.Idle
    }
}
