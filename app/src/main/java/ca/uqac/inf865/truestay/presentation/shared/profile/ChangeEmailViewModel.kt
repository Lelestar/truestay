package ca.uqac.inf865.truestay.presentation.shared.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangeEmailUiState(
    val currentEmail: String = "",
    val newEmail: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val newEmailError: String? = null,
    val passwordError: String? = null,
    val submitErrorMessage: String? = null,
    val submitSuccess: Boolean = false
)

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeEmailUiState(isLoading = true))
    val uiState: StateFlow<ChangeEmailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentEmail()
    }

    private fun loadCurrentEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentEmail = user?.email.orEmpty(),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun onNewEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                newEmail = email,
                newEmailError = null,
                submitSuccess = false,
                submitErrorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                submitSuccess = false,
                submitErrorMessage = null
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
                    submitErrorMessage = null,
                    submitSuccess = false
                )
            }

            authRepository.updateEmail(state.newEmail, state.password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            currentEmail = state.newEmail,
                            newEmail = "",
                            password = "",
                            newEmailError = null,
                            passwordError = null,
                            submitErrorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitErrorMessage = e.message,
                            submitSuccess = false
                        )
                    }
                }
        }
    }

    private fun validateInputs(): Boolean {
        var hasError = false

        if (uiState.value.newEmail.isBlank()) {
            _uiState.update {
                it.copy(newEmailError = context.getString(R.string.register_error_email_required))
            }
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.value.newEmail).matches()) {
            _uiState.update {
                it.copy(newEmailError = context.getString(R.string.register_error_email_invalid))
            }
            hasError = true
        } else if (uiState.value.newEmail == uiState.value.currentEmail) {
            _uiState.update {
                it.copy(newEmailError = context.getString(R.string.auth_error_invalid_credential))
            }
            hasError = true
        }

        if (uiState.value.password.isBlank()) {
            _uiState.update {
                it.copy(passwordError = context.getString(R.string.login_error_password_required))
            }
            hasError = true
        }

        return !hasError
    }
}

