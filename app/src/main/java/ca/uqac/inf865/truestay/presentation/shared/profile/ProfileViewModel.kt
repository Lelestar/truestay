package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.repository.ThemePreferencesRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: UserRepository,
    private val userRepository: UserRepository,
    private val repository: ThemePreferencesRepository
) : ViewModel() {
    val isDarkTheme: Flow<Boolean> = repository.isDarkTheme

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(isDark)
        }
    }

    fun updateUser(userId: String, firstName: String, lastName: String, email: String, phoneNumber: String) {
        viewModelScope.launch {
            val updatedUser = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phoneNumber" to phoneNumber
            )

            val result = userRepository.updateUserFields(userId, updatedUser)

            if (result.isSuccess) {
                // Tu peux ajouter un message succès si besoin
            } else {
                // Gérer erreur si besoin
            }
        }
    }

    // TODO: Implement profile logic
}