package ca.uqac.inf865.truestay.domain.usecase.profile

import android.net.Uri
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Uploads a new profile picture for the given user and updates the user document.
 *
 * @return Result containing the new profile picture URL on success.
 */
class UpdateProfilePictureUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(userId: String, imageUri: Uri): Result<String> {
        return try {
            val path = "users/$userId/profile"
            val url = storageRepository.uploadImage(imageUri, path).getOrThrow()
            userRepository.updateUserFields(userId, mapOf("profilePictureUrl" to url)).getOrThrow()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

