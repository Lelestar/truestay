package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.ElementType
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomElement
import ca.uqac.inf865.truestay.domain.model.RoomType
import com.google.firebase.firestore.DocumentSnapshot

data class PropertyDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val address: AddressDto = AddressDto(),
    val monthlyRent: Int = 0,
    val surface: Int = 0,
    val rooms: List<RoomDto> = emptyList(),
    val photos: List<String> = emptyList(),
    val landlordId: String = "",
    val isInBuilding: Boolean = true,
    val isAvailable: Boolean = true,
    val status: String = "draft",
    val ratings: PropertyRatingsDto = PropertyRatingsDto(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class AddressDto(
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val province: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class PropertyRatingsDto(
    val propertyAverageRating: Float = 0f,
    val propertyReviewCount: Int = 0,
    val buildingAverageRating: Float = 0f,
    val buildingReviewCount: Int = 0,
    val neighborhoodAverageRating: Float = 0f,
    val neighborhoodReviewCount: Int = 0
)

data class RoomDto(
    val id: String = "",
    val name: String = "",
    val type: String = "other",
    val elements: List<RoomElementDto> = emptyList()
)

data class RoomElementDto(
    val id: String = "",
    val type: String = "wall"
)

// DTO -> Domain Conversion
fun PropertyDto.toDomain(averageRating: Float = 0f): Property {
    return Property(
        id = id,
        name = name,
        description = description,
        address = address.toDomain(),
        monthlyRent = monthlyRent,
        surface = surface,
        rooms = rooms.map { it.toDomain() },
        photos = photos,
        landlordId = landlordId,
        isInBuilding = isInBuilding,
        isAvailable = isAvailable,
        status = when (status) {
            "published" -> PropertyStatus.PUBLISHED
            "paused" -> PropertyStatus.PAUSED
            "archived" -> PropertyStatus.ARCHIVED
            else -> PropertyStatus.DRAFT
        },
        ratings.toDomain(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AddressDto.toDomain(): Address {
    return Address(
        street = street,
        city = city,
        postalCode = postalCode,
        province = province,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}

fun PropertyRatingsDto.toDomain(): PropertyRatings {
    return PropertyRatings(
        propertyAverageRating = propertyAverageRating,
        propertyReviewCount = propertyReviewCount,
        buildingAverageRating = buildingAverageRating,
        buildingReviewCount = buildingReviewCount,
        neighborhoodAverageRating = neighborhoodAverageRating,
        neighborhoodReviewCount = neighborhoodReviewCount
    )
}

fun RoomDto.toDomain(): Room {
    return Room(
        id = id,
        type = when (type) {
            "bedroom" -> RoomType.BEDROOM
            "living_room" -> RoomType.LIVING_ROOM
            "kitchen" -> RoomType.KITCHEN
            "bathroom" -> RoomType.BATHROOM
            "toilet" -> RoomType.TOILET
            "entrance" -> RoomType.ENTRANCE
            "hallway" -> RoomType.HALLWAY
            "dining_room" -> RoomType.DINING_ROOM
            "office" -> RoomType.OFFICE
            "laundry_room" -> RoomType.LAUNDRY_ROOM
            "storage_room" -> RoomType.STORAGE_ROOM
            "garage" -> RoomType.GARAGE
            "basement" -> RoomType.BASEMENT
            "attic" -> RoomType.ATTIC
            "balcony" -> RoomType.BALCONY
            "terrace" -> RoomType.TERRACE
            "garden" -> RoomType.GARDEN
            "veranda" -> RoomType.VERANDA
            "staircase" -> RoomType.STAIRCASE
            else -> RoomType.OTHER
        },
        elements = elements.map { it.toDomain() }
    )
}

fun RoomElementDto.toDomain(): RoomElement {
    return RoomElement(
        id = id,
        type = when (type) {
            "floor" -> ElementType.FLOOR
            "wall" -> ElementType.WALL
            "ceiling" -> ElementType.CEILING
            "window" -> ElementType.WINDOW
            "door" -> ElementType.DOOR
            "furniture" -> ElementType.FURNITURE
            "equipment" -> ElementType.EQUIPMENT
            else -> ElementType.WALL
        }
    )
}

// Domain -> DTO Conversion
fun Property.toDto(): PropertyDto {
    return PropertyDto(
        id = id,
        name = name,
        description = description,
        address = address.toDto(),
        monthlyRent = monthlyRent,
        surface = surface,
        rooms = rooms.map { it.toDto() },
        photos = photos,
        landlordId = landlordId,
        isInBuilding = isInBuilding,
        isAvailable = isAvailable,
        status = when (status) {
            PropertyStatus.PUBLISHED -> "published"
            PropertyStatus.PAUSED -> "paused"
            PropertyStatus.ARCHIVED -> "archived"
            PropertyStatus.DRAFT -> "draft"
        },
        ratings = ratings.toDto(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Address.toDto(): AddressDto {
    return AddressDto(
        street = street,
        city = city,
        postalCode = postalCode,
        province = province,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}

fun PropertyRatings.toDto(): PropertyRatingsDto {
    return PropertyRatingsDto(
        propertyAverageRating = propertyAverageRating,
        propertyReviewCount = propertyReviewCount,
        buildingAverageRating = buildingAverageRating,
        buildingReviewCount = buildingReviewCount,
        neighborhoodAverageRating = neighborhoodAverageRating,
        neighborhoodReviewCount = neighborhoodReviewCount
    )
}

fun Room.toDto(): RoomDto {
    return RoomDto(
        id = id,
        type = when (type) {
            RoomType.BEDROOM -> "bedroom"
            RoomType.LIVING_ROOM -> "living_room"
            RoomType.KITCHEN -> "kitchen"
            RoomType.BATHROOM -> "bathroom"
            RoomType.TOILET -> "toilet"
            RoomType.ENTRANCE -> "entrance"
            RoomType.HALLWAY -> "hallway"
            RoomType.DINING_ROOM -> "dining_room"
            RoomType.OFFICE -> "office"
            RoomType.LAUNDRY_ROOM -> "laundry_room"
            RoomType.STORAGE_ROOM -> "storage_room"
            RoomType.GARAGE -> "garage"
            RoomType.BASEMENT -> "basement"
            RoomType.ATTIC -> "attic"
            RoomType.BALCONY -> "balcony"
            RoomType.TERRACE -> "terrace"
            RoomType.GARDEN -> "garden"
            RoomType.VERANDA -> "veranda"
            RoomType.STAIRCASE -> "staircase"
            RoomType.OTHER -> "other"
        },
        elements = elements.map { it.toDto() }
    )
}

fun RoomElement.toDto(): RoomElementDto {
    return RoomElementDto(
        id = id,
        type = when (type) {
            ElementType.FLOOR -> "floor"
            ElementType.WALL -> "wall"
            ElementType.CEILING -> "ceiling"
            ElementType.WINDOW -> "window"
            ElementType.DOOR -> "door"
            ElementType.FURNITURE -> "furniture"
            ElementType.EQUIPMENT -> "equipment"
        }
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toPropertyDto(): PropertyDto? {
    return try {
        toObject(PropertyDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}