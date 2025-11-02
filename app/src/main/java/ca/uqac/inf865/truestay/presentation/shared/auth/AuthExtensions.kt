package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.domain.model.User

/**
 * Retrieves the currently authenticated user
 * @return The logged-in user or null if not authenticated
 */
@Composable
fun rememberCurrentUser(
    authViewModel: AuthViewModel = hiltViewModel()
): User? {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    return (authState as? AuthState.Authenticated)?.user
}