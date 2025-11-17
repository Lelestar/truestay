package ca.uqac.inf865.truestay.presentation.shared.profile

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val isSubmitting: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val globalError: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                currentPassword = password,
                currentPasswordError = null,
                globalError = null,
                successMessage = null
            )
        }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                newPasswordError = null,
                globalError = null,
                successMessage = null
            )
        }
    }

    fun submitChange() {
        if (!validateInputs()) return
        val state = uiState.value

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    globalError = null,
                    successMessage = null
                )
            }

            authRepository.updatePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = context.getString(R.string.change_password_success),
                            currentPassword = "",
                            newPassword = "",
                            currentPasswordError = null,
                            newPasswordError = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            globalError = mapErrorToMessage(error)
                        )
                    }
                }
        }
    }

    private fun validateInputs(): Boolean {
        var hasError = false
        val state = uiState.value

        if (state.currentPassword.isBlank()) {
            _uiState.update {
                it.copy(currentPasswordError = context.getString(R.string.login_error_password_required))
            }
            hasError = true
        }

        if (state.newPassword.isBlank()) {
            _uiState.update {
                it.copy(newPasswordError = context.getString(R.string.register_error_password_required))
            }
            hasError = true
        } else if (state.newPassword.length < 8) {
            _uiState.update {
                it.copy(newPasswordError = context.getString(R.string.register_error_password_min_length))
            }
            hasError = true
        }

        if (hasError) return false
        if (state.newPassword == state.currentPassword) {
            _uiState.update {
                it.copy(newPasswordError = context.getString(R.string.auth_error_weak_password))
            }
            return false
        }

        return true
    }

    private fun mapErrorToMessage(error: Throwable): String {
        return when {
            error is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_WRONG_PASSWORD" ->
                        context.getString(R.string.auth_error_wrong_password)
                    "ERROR_WEAK_PASSWORD" ->
                        context.getString(R.string.auth_error_weak_password)
                    "ERROR_TOO_MANY_REQUESTS" ->
                        context.getString(R.string.auth_error_too_many_requests)
                    "ERROR_NETWORK_REQUEST_FAILED" ->
                        context.getString(R.string.auth_error_network)
                    else ->
                        context.getString(R.string.auth_error_generic)
                }
            }
            error.message?.contains("network", ignoreCase = true) == true ->
                context.getString(R.string.auth_error_network)
            else ->
                error.message ?: context.getString(R.string.auth_error_generic)
        }
    }
}

