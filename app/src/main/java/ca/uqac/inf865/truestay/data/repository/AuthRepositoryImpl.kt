package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.model.UserDto
import ca.uqac.inf865.truestay.data.source.AuthDataSource
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signIn(email, password)
                ?: return Result.failure(Exception("Login failed"))

            val userDto = firestoreDataSource.getDocument(
                "users",
                firebaseUser.uid,
                UserDto::class.java
            ) ?: return Result.failure(Exception("User not found"))

            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        role: UserRole
    ): Result<User> {
        return try {
            val firebaseUser = authDataSource.signUp(email, password)
                ?: return Result.failure(Exception("Registration failed"))

            val user = User(
                id = firebaseUser.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                role = role,
                phoneNumber = phoneNumber,
                createdAt = System.currentTimeMillis()
            )

            firestoreDataSource.updateDocument("users", user.id, user.toDto())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = authDataSource.getCurrentUser()
            if (firebaseUser == null) {
                return Result.success(null)
            }

            val userDto = firestoreDataSource.getDocument(
                "users",
                firebaseUser.uid,
                UserDto::class.java
            )

            Result.success(userDto?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            authDataSource.resetPassword(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Re-authenticates the user before a sensitive operation
     * (required before changing email or password)
     */
    override suspend fun reauthenticate(password: String): Result<Unit> {
        return try {
            val currentUser = authDataSource.getCurrentUser()
                ?: return Result.failure(Exception("User not logged in"))

            val email = currentUser.email
                ?: return Result.failure(Exception("No email found"))

            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.reauthenticate(credential).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change the email in Firebase Auth
     * IMPORTANT: Requires recent re-authentication
     */
    override suspend fun updateEmail(newEmail: String, currentPassword: String): Result<Unit> {
        return try {
            val currentUser = authDataSource.getCurrentUser()
                ?: return Result.failure(Exception("User not logged in"))

            // 1. Re-authenticate (required for sensitive operations)
            reauthenticate(currentPassword).getOrThrow()

            // 2. Send verification email to new address and update in Firebase Auth
            currentUser.verifyBeforeUpdateEmail(newEmail).await()

            // TODO: update email in Firestore user document

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Change the password in Firebase Auth
     * IMPORTANT: Requires recent re-authentication
     */
    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val currentUser = authDataSource.getCurrentUser()
                ?: return Result.failure(Exception("User not logged in"))

            // 1. Re-authenticate (required for sensitive operations)
            reauthenticate(currentPassword).getOrThrow()

            // 2. Update password in Firebase Auth
            currentUser.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}