package com.example.demo2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo2.data.model.User
import com.example.demo2.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserState())
    val uiState: StateFlow<UserState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.login(email, password)
                    .onSuccess { user ->
                        _uiState.value = UserState(user = user, isLoggedIn = true)
                    }
                    .onFailure { exception ->
                        _uiState.value = UserState(error = exception.message)
                    }
            } catch (e: Exception) {
                _uiState.value = UserState(error = e.message)
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.register(user)
                    .onSuccess { registeredUser ->
                        _uiState.value = UserState(user = registeredUser, isLoggedIn = true)
                    }
                    .onFailure { exception ->
                        _uiState.value = UserState(error = exception.message)
                    }
            } catch (e: Exception) {
                _uiState.value = UserState(error = e.message)
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.updateUser(user)
                    .onSuccess { updatedUser ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = updatedUser
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = UserState()
        }
    }
} 