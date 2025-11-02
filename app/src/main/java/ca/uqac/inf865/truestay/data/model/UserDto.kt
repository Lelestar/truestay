package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot

data class UserDto(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "tenant",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val createdAt: Long = 0L,
    val twoFactorEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true
)

// DTO -> Domain Conversion
fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = when (role) {
            "landlord" -> UserRole.LANDLORD
            else -> UserRole.TENANT
        },
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        createdAt = createdAt,
        twoFactorEnabled = twoFactorEnabled,
        emailNotificationsEnabled = emailNotificationsEnabled,
        pushNotificationsEnabled = pushNotificationsEnabled
    )
}

// Domain -> DTO Conversion
fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = when (role) {
            UserRole.LANDLORD -> "landlord"
            UserRole.TENANT -> "tenant"
        },
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        createdAt = createdAt,
        twoFactorEnabled = twoFactorEnabled,
        emailNotificationsEnabled = emailNotificationsEnabled,
        pushNotificationsEnabled = pushNotificationsEnabled
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toUserDto(): UserDto? {
    return try {
        toObject(UserDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}