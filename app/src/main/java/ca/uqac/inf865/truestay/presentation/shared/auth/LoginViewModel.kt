package ca.uqac.inf865.truestay.presentation.shared.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            globalError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            globalError = null
        )
    }

    fun login() {
        // Validation
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.login(_uiState.value.email, _uiState.value.password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = user.role
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        globalError = mapErrorToMessage(error)
                    )
                }
        }
    }

    private fun validateInputs(): Boolean {
        var hasError = false

        // Email validation
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.login_error_email_required)
            )
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.login_error_email_invalid)
            )
            hasError = true
        }

        // Password validation
        if (_uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                passwordError = context.getString(R.string.login_error_password_required)
            )
            hasError = true
        } else if (_uiState.value.password.length < 8) {
            _uiState.value = _uiState.value.copy(
                passwordError = context.getString(R.string.login_error_password_min_length)
            )
            hasError = true
        }

        return !hasError
    }

    /**
     * Converts Firebase errors into user-friendly messages
     */
    private fun mapErrorToMessage(error: Throwable): String {
        return when {
            error is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                    "ERROR_WRONG_PASSWORD" -> context.getString(R.string.auth_error_wrong_password)
                    "ERROR_USER_NOT_FOUND" -> context.getString(R.string.auth_error_user_not_found)
                    "ERROR_USER_DISABLED" -> context.getString(R.string.auth_error_user_disabled)
                    "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                    "ERROR_NETWORK_REQUEST_FAILED" -> context.getString(R.string.auth_error_network)
                    "ERROR_INVALID_CREDENTIAL" -> context.getString(R.string.auth_error_invalid_credential)
                    else -> context.getString(R.string.auth_error_login, error.message)
                }
            }
            error.message?.contains("network", ignoreCase = true) == true ->
                context.getString(R.string.auth_error_network)
            else ->
                error.message ?: context.getString(R.string.auth_error_generic)
        }
    }

    fun onLoginSuccessHandled() {
        _uiState.value = _uiState.value.copy(loginSuccess = null)
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val globalError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: UserRole? = null
)