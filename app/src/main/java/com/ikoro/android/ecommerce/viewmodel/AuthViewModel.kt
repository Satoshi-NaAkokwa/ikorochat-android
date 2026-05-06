package com.ikoro.android.ecommerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikoro.android.ecommerce.data.model.User
import com.ikoro.android.ecommerce.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel
 * Handles authentication state and operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun register(publicKey: String, deviceInfo: Map<String, Any>) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                val result = authRepository.register(publicKey, deviceInfo)
                _authState.value = AuthState.Success(
                    user = result.user,
                    token = result.token
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(publicKey: String, challenge: String, signature: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                val result = authRepository.login(publicKey, challenge, signature)
                _authState.value = AuthState.Success(
                    user = result.user,
                    token = result.token
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}