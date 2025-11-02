package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.UserDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val user = firestoreDataSource.getDocument("users", userId, UserDto::class.java)
            Result.success(user?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val users = firestoreDataSource.queryDocuments(
                collection = "users",
                field = "email",
                value = email,
                clazz = UserDto::class.java
            )
            Result.success(users.firstOrNull()?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestoreDataSource.updateDocument("users", user.id, user.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkEmailExists(email: String): Result<Boolean> {
        return try {
            val user = getUserByEmail(email).getOrNull()
            Result.success(user != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}