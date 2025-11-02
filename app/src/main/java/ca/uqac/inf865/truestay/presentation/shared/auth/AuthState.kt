package ca.uqac.inf865.truestay.presentation.shared.auth

import ca.uqac.inf865.truestay.domain.model.User

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}