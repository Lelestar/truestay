package ca.uqac.inf865.truestay.presentation.shared.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            globalError = null
        )
    }

    fun sendPasswordResetEmail() {
        // Validation
        if (!validateEmail()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.resetPassword(_uiState.value.email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emailSent = true
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

    private fun validateEmail(): Boolean {
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.login_error_email_required)
            )
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.login_error_email_invalid)
            )
            return false
        }
        return true
    }

    private fun mapErrorToMessage(error: Throwable): String {
        return when {
            error is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                    "ERROR_USER_NOT_FOUND" -> context.getString(R.string.auth_error_user_not_found)
                    "ERROR_USER_DISABLED" -> context.getString(R.string.auth_error_user_disabled)
                    "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                    "ERROR_NETWORK_REQUEST_FAILED" -> context.getString(R.string.auth_error_network)
                    else -> context.getString(R.string.auth_error_generic)
                }
            }
            error.message?.contains("network", ignoreCase = true) == true ->
                context.getString(R.string.auth_error_network)
            else ->
                error.message ?: context.getString(R.string.auth_error_generic)
        }
    }

    fun onEmailSentHandled() {
        _uiState.value = _uiState.value.copy(emailSent = false)
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val globalError: String? = null,
    val emailSent: Boolean = false
)
