package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    // TODO: Implement profile logic
}