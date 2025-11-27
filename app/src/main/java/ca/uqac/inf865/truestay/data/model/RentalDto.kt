package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import com.google.firebase.firestore.DocumentSnapshot

data class RentalDto(
    val id: String = "",
    val propertyId: String = "",
    val tenantId: String = "",
    val landlordId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: String = "pending",
    val entryInventoryId: String? = null,
    val exitInventoryId: String? = null,
    val createdAt: Long = 0L,
    val acceptedAt: Long? = null
)

// DTO -> Domain Conversion
fun RentalDto.toDomain(): Rental {
    return Rental(
        id = id,
        propertyId = propertyId,
        tenantId = tenantId,
        landlordId = landlordId,
        startDate = startDate,
        endDate = endDate,
        status = when (status) {
            "pending" -> RentalStatus.PENDING
            "active" -> RentalStatus.ACTIVE
            "ended" -> RentalStatus.ENDED
            "cancelled" -> RentalStatus.CANCELLED
            else -> RentalStatus.PENDING
        },
        entryInventoryId = entryInventoryId,
        exitInventoryId = exitInventoryId,
        createdAt = createdAt,
        acceptedAt = acceptedAt
    )
}

// Domain -> DTO Conversion
fun Rental.toDto(): RentalDto {
    return RentalDto(
        id = id,
        propertyId = propertyId,
        tenantId = tenantId,
        landlordId = landlordId,
        startDate = startDate,
        endDate = endDate,
        status = when (status) {
            RentalStatus.PENDING -> "pending"
            RentalStatus.ACTIVE -> "active"
            RentalStatus.ENDED -> "ended"
            RentalStatus.CANCELLED -> "cancelled"
        },
        entryInventoryId = entryInventoryId,
        exitInventoryId = exitInventoryId,
        createdAt = createdAt,
        acceptedAt = acceptedAt
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toRentalDto(): RentalDto? {
    return try {
        toObject(RentalDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}