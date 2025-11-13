package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // TODO: Implement change password logic
}

