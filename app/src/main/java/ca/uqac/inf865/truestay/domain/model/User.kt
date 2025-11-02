package ca.uqac.inf865.truestay.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: UserRole = UserRole.TENANT,
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val createdAt: Long = 0L,
    val twoFactorEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true
)

enum class UserRole {
    TENANT,
    LANDLORD
}