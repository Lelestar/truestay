package ca.uqac.inf865.truestay.domain.model

data class Inventory(
    val id: String = "",
    val rentalId: String = "",
    val type: InventoryType = InventoryType.ENTRY,
    val rooms: List<InventoryRoom> = emptyList(),
    val landlordSignature: Signature? = null,
    val tenantSignature: Signature? = null,
    val status: InventoryStatus = InventoryStatus.DRAFT,
    val pdfUrl: String? = null,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

data class InventoryRoom(
    val roomId: String = "",
    val roomName: String = "", // Denormalized for easier display
    val elements: List<InventoryElement> = emptyList(),
    val status: RoomInventoryStatus = RoomInventoryStatus.TODO,
    val photoUrls: List<String> = emptyList()
)

data class InventoryElement(
    val elementId: String = "",
    val elementName: String = "", // Denormalized for easier display
    val condition: ElementCondition = ElementCondition.TO_CHECK,
    val comment: String = "",
    val photoUrls: List<String> = emptyList()
)

enum class ElementCondition {
    GOOD,
    TO_CHECK,
    DAMAGED,
    NOT_APPLICABLE
}

data class Signature(
    val userId: String = "",
    val signatureImageUrl: String = "",
    val signedAt: Long = 0L
)

enum class InventoryType {
    ENTRY,
    EXIT
}

enum class InventoryStatus {
    DRAFT,
    IN_PROGRESS,
    PENDING_SIGNATURE,
    SIGNED,
    COMPLETED,
    CANCELLED
}

enum class RoomInventoryStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED
}