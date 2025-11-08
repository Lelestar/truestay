package ca.uqac.inf865.truestay.domain.model

data class Property(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val address: Address = Address(),
    val monthlyRent: Int = 0,
    val surface: Int = 0,
    val rooms: List<Room> = emptyList(),
    val photos: List<String> = emptyList(),
    val landlordId: String = "",
    val isInBuilding: Boolean = true,
    val isAvailable: Boolean = true, // Denormalized for quick access, updated on rental update with cloud functions
    val status: PropertyStatus = PropertyStatus.DRAFT,
    val ratings: PropertyRatings = PropertyRatings(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class Address(
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val province: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class PropertyRatings(
    // Property ratings
    val propertyAverageRating: Float = 0f,
    val propertyReviewCount: Int = 0,

    // Building ratings
    val buildingAverageRating: Float = 0f,
    val buildingReviewCount: Int = 0,

    // Neighborhood ratings
    val neighborhoodAverageRating: Float = 0f,
    val neighborhoodReviewCount: Int = 0
)

data class Room(
    val id: String = "",
    val name: String = "",
    val type: RoomType = RoomType.OTHER,
    val elements: List<RoomElement> = emptyList()
)

enum class RoomType {
    BEDROOM,
    LIVING_ROOM,
    KITCHEN,
    BATHROOM,
    TOILET,
    ENTRANCE,
    HALLWAY,
    DINING_ROOM,
    OFFICE,
    LAUNDRY_ROOM,
    STORAGE_ROOM,
    GARAGE,
    BASEMENT,
    ATTIC,
    BALCONY,
    TERRACE,
    GARDEN,
    VERANDA,
    STAIRCASE,
    OTHER
}

data class RoomElement(
    val id: String = "",
    val type: ElementType = ElementType.WALL
)

enum class ElementType {
    FLOOR,
    WALL,
    CEILING,
    WINDOW,
    DOOR,
    FURNITURE,
    EQUIPMENT
}

enum class PropertyStatus {
    DRAFT,
    PUBLISHED,
    PAUSED,
    ARCHIVED
}