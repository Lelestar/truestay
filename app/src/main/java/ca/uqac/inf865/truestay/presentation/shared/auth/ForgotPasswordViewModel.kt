package ca.uqac.inf865.truestay.presentation.shared.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    // TODO: Implement forgot password logic here
}