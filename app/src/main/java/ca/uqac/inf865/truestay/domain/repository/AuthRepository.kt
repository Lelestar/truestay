package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        role: UserRole
    ): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateEmail(newEmail: String, currentPassword: String): Result<Unit>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun reauthenticate(password: String): Result<Unit>
}