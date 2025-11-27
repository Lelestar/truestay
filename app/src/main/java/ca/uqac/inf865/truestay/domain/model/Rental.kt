package ca.uqac.inf865.truestay.domain.model

data class Rental(
    val id: String = "",
    val propertyId: String = "",
    val tenantId: String = "",
    val landlordId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: RentalStatus = RentalStatus.PENDING,
    val entryInventoryId: String? = null,
    val exitInventoryId: String? = null,
    val createdAt: Long = 0L,
    val acceptedAt: Long? = null
)

enum class RentalStatus {
    PENDING, // The rental request is pending approval
    ACTIVE,
    ENDED,
    CANCELLED
}