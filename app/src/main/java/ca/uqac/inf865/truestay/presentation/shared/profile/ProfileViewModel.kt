package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.User
import android.net.Uri
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.ThemePreferencesRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import ca.uqac.inf865.truestay.domain.usecase.profile.UpdateProfilePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the profile screen.
 */
data class ProfileUiState(
    val user: User? = null,
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailVerified: Boolean = false,
    val profileUpdateErrorRes: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val updateProfilePictureUseCase: UpdateProfilePictureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.getCurrentUser()
                .onSuccess { authUser ->
                    if (authUser == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = null
                            )
                        }
                        return@onSuccess
                    }
                    // Reload fresh user data from repository
                    userRepository.getUserById(authUser.id)
                        .onSuccess { fullUser ->
                            val isDark = themePreferencesRepository.isDarkTheme.first()
                            val isEmailVerified = authRepository.isEmailVerified().getOrDefault(false)
                            // Always trust auth for email, but keep extended fields from Firestore user
                            val mergedUser = fullUser?.copy(email = authUser.email) ?: fullUser
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    user = mergedUser,
                                    isDarkTheme = isDark,
                                    isEmailVerified = isEmailVerified,
                                    errorMessage = null
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    // Keep previous user (extended fields) if any; otherwise leave as is
                                    user = it.user,
                                    errorMessage = e.message
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = null,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setDarkTheme(isDark)
            _uiState.update { it.copy(isDarkTheme = isDark) }
        }
    }

    fun updateNotificationPreferences(
        pushEnabled: Boolean? = null,
        emailEnabled: Boolean? = null
    ) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch {
            val fields = mutableMapOf<String, Any?>()
            pushEnabled?.let { fields["pushNotificationsEnabled"] = it }
            emailEnabled?.let { fields["emailNotificationsEnabled"] = it }
            if (fields.isEmpty()) return@launch

            userRepository.updateUserFields(currentUser.id, fields)
                .onSuccess {
                    // Update local state to reflect new values
                    val updatedUser = currentUser.copy(
                        pushNotificationsEnabled = pushEnabled ?: currentUser.pushNotificationsEnabled,
                        emailNotificationsEnabled = emailEnabled ?: currentUser.emailNotificationsEnabled
                    )
                    _uiState.update { it.copy(user = updatedUser, profileUpdateErrorRes = null) }
                }
                .onFailure {
                    _uiState.update { it.copy(profileUpdateErrorRes = R.string.profile_update_error) }
                }
        }
    }

    fun updateTwoFactorEnabled(enabled: Boolean) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch {
            userRepository.updateUserFields(currentUser.id, mapOf("twoFactorEnabled" to enabled))
                .onSuccess {
                    val updatedUser = currentUser.copy(twoFactorEnabled = enabled)
                    _uiState.update { it.copy(user = updatedUser, profileUpdateErrorRes = null) }
                }
                .onFailure {
                    _uiState.update { it.copy(profileUpdateErrorRes = R.string.profile_update_error) }
                }
        }
    }

    fun clearProfileUpdateError() {
        _uiState.update { it.copy(profileUpdateErrorRes = null) }
    }

    fun updateProfilePicture(imageUri: Uri) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch {
            updateProfilePictureUseCase(currentUser.id, imageUri)
                .onSuccess { url ->
                    val updatedUser = currentUser.copy(profilePictureUrl = url)
                    _uiState.update { it.copy(user = updatedUser, profileUpdateErrorRes = null) }
                }
                .onFailure {
                    _uiState.update { it.copy(profileUpdateErrorRes = R.string.profile_update_error) }
                }
        }
    }
}
