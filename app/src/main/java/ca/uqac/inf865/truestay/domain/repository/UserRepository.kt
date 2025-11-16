package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.User

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getUserByEmail(email: String): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun checkEmailExists(email: String): Result<Boolean>
    suspend fun updateUserFields(userId: String, fields: Map<String, Any?>): Result<Unit>

}