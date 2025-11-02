package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _authState.value = if (user != null) {
                        AuthState.Authenticated(user)
                    } else {
                        AuthState.Unauthenticated
                    }
                }
                .onFailure {
                    _authState.value = AuthState.Unauthenticated
                }
        }
    }

    fun login(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    onResult(Result.success(user))
                }
                .onFailure { error ->
                    onResult(Result.failure(error))
                }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        role: UserRole,
        onResult: (Result<User>) -> Unit
    ) {
        viewModelScope.launch {
            authRepository.register(email, password, firstName, lastName, phoneNumber, role)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    onResult(Result.success(user))
                }
                .onFailure { error ->
                    onResult(Result.failure(error))
                }
        }
    }

    fun resetPassword(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    onResult(Result.success(Unit))
                }
                .onFailure { error ->
                    onResult(Result.failure(error))
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun refreshAuthStatus() {
        checkAuthStatus()
    }
}