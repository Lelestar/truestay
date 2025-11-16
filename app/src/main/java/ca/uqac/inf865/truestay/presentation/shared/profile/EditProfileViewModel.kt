package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveErrorMessage: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = null
                            )
                        }
                        return@onSuccess
                    }
                    userRepository.getUserById(user.id)
                        .onSuccess { fullUser ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    user = fullUser ?: user,
                                    errorMessage = null
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    user = user,
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

    fun saveProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveErrorMessage = null,
                    saveSuccess = false
                )
            }

            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber
            )

            userRepository.updateUser(updatedUser)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            isSaving = false,
                            saveSuccess = true,
                            saveErrorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = false,
                            saveErrorMessage = e.message
                        )
                    }
                }
        }
    }
}

