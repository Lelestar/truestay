package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.ElementCondition
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.InventoryElement
import ca.uqac.inf865.truestay.domain.model.InventoryRoom
import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.model.InventoryType
import ca.uqac.inf865.truestay.domain.model.RoomInventoryStatus
import ca.uqac.inf865.truestay.domain.model.Signature
import com.google.firebase.firestore.DocumentSnapshot

data class InventoryDto(
    val id: String = "",
    val rentalId: String = "",
    val type: String = "entry",
    val rooms: List<InventoryRoomDto> = emptyList(),
    val landlordSignature: SignatureDto? = null,
    val tenantSignature: SignatureDto? = null,
    val status: String = "draft",
    val pdfUrl: String? = null,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

data class InventoryRoomDto(
    val roomId: String = "",
    val roomName: String = "",
    val elements: List<InventoryElementDto> = emptyList(),
    val status: String = "todo",
    val photoUrls: List<String> = emptyList()
)

data class InventoryElementDto(
    val elementId: String = "",
    val elementName: String = "",
    val condition: String = "good",
    val comment: String = "",
    val photoUrls: List<String> = emptyList()
)

data class SignatureDto(
    val userId: String = "",
    val signatureImageUrl: String = "",
    val signedAt: Long = 0L
)

// DTO -> Domain Conversion
fun InventoryDto.toDomain(): Inventory {
    return Inventory(
        id = id,
        rentalId = rentalId,
        type = when (type) {
            "exit" -> InventoryType.EXIT
            else -> InventoryType.ENTRY
        },
        rooms = rooms.map { it.toDomain() },
        landlordSignature = landlordSignature?.toDomain(),
        tenantSignature = tenantSignature?.toDomain(),
        status = when (status) {
            "in_progress" -> InventoryStatus.IN_PROGRESS
            "pending_signature" -> InventoryStatus.PENDING_SIGNATURE
            "signed" -> InventoryStatus.SIGNED
            "completed" -> InventoryStatus.COMPLETED
            "cancelled" -> InventoryStatus.CANCELLED
            else -> InventoryStatus.DRAFT
        },
        pdfUrl = pdfUrl,
        createdAt = createdAt,
        completedAt = completedAt
    )
}

fun InventoryRoomDto.toDomain(): InventoryRoom {
    return InventoryRoom(
        roomId = roomId,
        roomName = roomName,
        elements = elements.map { it.toDomain() },
        status = when (status) {
            "in_progress" -> RoomInventoryStatus.IN_PROGRESS
            "completed" -> RoomInventoryStatus.COMPLETED
            else -> RoomInventoryStatus.TODO
        },
        photoUrls = photoUrls
    )
}

fun InventoryElementDto.toDomain(): InventoryElement {
    return InventoryElement(
        elementId = elementId,
        elementName = elementName,
        condition = when (condition) {
            "good" -> ElementCondition.GOOD
            "damaged" -> ElementCondition.DAMAGED
            else -> ElementCondition.TO_CHECK
        },
        comment = comment,
        photoUrls = photoUrls
    )
}

fun SignatureDto.toDomain(): Signature {
    return Signature(
        userId = userId,
        signatureImageUrl = signatureImageUrl,
        signedAt = signedAt
    )
}

// Domain -> DTO Conversion
fun Inventory.toDto(): InventoryDto {
    return InventoryDto(
        id = id,
        rentalId = rentalId,
        type = when (type) {
            InventoryType.EXIT -> "exit"
            InventoryType.ENTRY -> "entry"
        },
        rooms = rooms.map { it.toDto() },
        landlordSignature = landlordSignature?.toDto(),
        tenantSignature = tenantSignature?.toDto(),
        status = when (status) {
            InventoryStatus.DRAFT -> "draft"
            InventoryStatus.IN_PROGRESS -> "in_progress"
            InventoryStatus.PENDING_SIGNATURE -> "pending_signature"
            InventoryStatus.SIGNED -> "signed"
            InventoryStatus.COMPLETED -> "completed"
            InventoryStatus.CANCELLED -> "cancelled"
        },
        pdfUrl = pdfUrl,
        createdAt = createdAt,
        completedAt = completedAt
    )
}

fun InventoryRoom.toDto(): InventoryRoomDto {
    return InventoryRoomDto(
        roomId = roomId,
        roomName = roomName,
        elements = elements.map { it.toDto() },
        status = when (status) {
            RoomInventoryStatus.TODO -> "todo"
            RoomInventoryStatus.IN_PROGRESS -> "in_progress"
            RoomInventoryStatus.COMPLETED -> "completed"
        },
        photoUrls = photoUrls
    )
}

fun InventoryElement.toDto(): InventoryElementDto {
    return InventoryElementDto(
        elementId = elementId,
        elementName = elementName,
        condition = when (condition) {
            ElementCondition.GOOD -> "good"
            ElementCondition.TO_CHECK -> "to_check"
            ElementCondition.DAMAGED -> "damaged"
            ElementCondition.NOT_APPLICABLE -> "not_applicable"
        },
        comment = comment,
        photoUrls = photoUrls
    )
}

fun Signature.toDto(): SignatureDto {
    return SignatureDto(
        userId = userId,
        signatureImageUrl = signatureImageUrl,
        signedAt = signedAt
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toInventoryDto(): InventoryDto? {
    return try {
        toObject(InventoryDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}