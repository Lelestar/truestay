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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFirstNameChanged(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            firstNameError = null,
            globalError = null
        )
    }

    fun onLastNameChanged(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            lastNameError = null,
            globalError = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            globalError = null
        )
    }

    fun onPhoneChanged(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            phoneError = null,
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

    fun onPasswordConfirmChanged(passwordConfirm: String) {
        _uiState.value = _uiState.value.copy(
            passwordConfirm = passwordConfirm,
            passwordConfirmError = null,
            globalError = null
        )
    }

    fun register(userRole: UserRole) {
        // Validation
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.register(
                email = _uiState.value.email,
                password = _uiState.value.password,
                firstName = _uiState.value.firstName,
                lastName = _uiState.value.lastName,
                phoneNumber = _uiState.value.phone,
                role = userRole
            )
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registerSuccess = true
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

        // First name validation
        if (_uiState.value.firstName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                firstNameError = context.getString(R.string.register_error_first_name_required)
            )
            hasError = true
        }

        // Last name validation
        if (_uiState.value.lastName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                lastNameError = context.getString(R.string.register_error_last_name_required)
            )
            hasError = true
        }

        // Email validation
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.register_error_email_required)
            )
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.value = _uiState.value.copy(
                emailError = context.getString(R.string.register_error_email_invalid)
            )
            hasError = true
        }

        // Phone validation
        if (_uiState.value.phone.isBlank()) {
            _uiState.value = _uiState.value.copy(
                phoneError = context.getString(R.string.register_error_phone_required)
            )
            hasError = true
        } else if (!isValidPhoneNumber(_uiState.value.phone)) {
            _uiState.value = _uiState.value.copy(
                phoneError = context.getString(R.string.register_error_phone_invalid)
            )
            hasError = true
        }

        // Password validation
        if (_uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                passwordError = context.getString(R.string.register_error_password_required)
            )
            hasError = true
        } else if (_uiState.value.password.length < 8) {
            _uiState.value = _uiState.value.copy(
                passwordError = context.getString(R.string.register_error_password_min_length)
            )
            hasError = true
        }

        // Password confirmation validation
        if (_uiState.value.passwordConfirm.isBlank()) {
            _uiState.value = _uiState.value.copy(
                passwordConfirmError = context.getString(R.string.register_error_password_confirm_required)
            )
            hasError = true
        } else if (_uiState.value.password != _uiState.value.passwordConfirm) {
            _uiState.value = _uiState.value.copy(
                passwordConfirmError = context.getString(R.string.register_error_passwords_not_match)
            )
            hasError = true
        }

        return !hasError
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Remove all non-digit characters except +
        val cleanedPhone = phone.replace(Regex("[^0-9+]"), "")

        // International format: +[country code][number]
        // Must start with +, followed by country code (1-3 digits), then the number
        // Total: 8 to 15 digits (E.164 standard)
        val internationalPattern = Regex("^\\+[1-9][0-9]{7,14}$")

        return internationalPattern.matches(cleanedPhone)
    }

    /**
     * Converts Firebase errors into user-friendly messages
     */
    private fun mapErrorToMessage(error: Throwable): String {
        return when {
            error is FirebaseAuthException -> {
                when (error.errorCode) {
                    "ERROR_INVALID_EMAIL" -> context.getString(R.string.auth_error_invalid_email)
                    "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.auth_error_email_already_in_use)
                    "ERROR_WEAK_PASSWORD" -> context.getString(R.string.auth_error_weak_password)
                    "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.auth_error_too_many_requests)
                    "ERROR_NETWORK_REQUEST_FAILED" -> context.getString(R.string.auth_error_network)
                    else -> context.getString(R.string.auth_error_register, error.message ?: "")
                }
            }
            error.message?.contains("network", ignoreCase = true) == true ->
                context.getString(R.string.auth_error_network)
            else ->
                error.message ?: context.getString(R.string.auth_error_generic)
        }
    }

    fun onRegisterSuccessHandled() {
        _uiState.value = _uiState.value.copy(registerSuccess = false)
    }
}

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null,
    val globalError: String? = null,
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false
)
