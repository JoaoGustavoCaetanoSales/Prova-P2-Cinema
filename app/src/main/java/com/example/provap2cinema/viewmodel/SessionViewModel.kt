package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Session
import com.example.provap2cinema.repository.SessionRepository
import kotlinx.coroutines.launch

sealed class SessionState {
    object Idle : SessionState()
    object Loading : SessionState()
    data class Success(val sessions: List<Session>) : SessionState()
    data class Error(val message: String) : SessionState()
}

class SessionViewModel(private val repository: SessionRepository = SessionRepository()) : ViewModel() {

    var sessionState by mutableStateOf<SessionState>(SessionState.Idle)
        private set

    init {
        fetchSessions()
    }

    fun fetchSessions() {
        viewModelScope.launch {
            sessionState = SessionState.Loading
            repository.getSessions()
                .onSuccess { sessions -> sessionState = SessionState.Success(sessions) }
                .onFailure { error -> sessionState = SessionState.Error(error.message ?: "Erro ao carregar sessões") }
        }
    }

    fun addSession(session: Session, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addSession(session)
                .onSuccess {
                    fetchSessions()
                    onSuccess()
                }
        }
    }

    fun updateSession(session: Session, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateSession(session)
                .onSuccess {
                    fetchSessions()
                    onSuccess()
                }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
                .onSuccess { fetchSessions() }
        }
    }
}
