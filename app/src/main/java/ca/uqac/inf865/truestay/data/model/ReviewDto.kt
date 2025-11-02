package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.BuildingReview
import ca.uqac.inf865.truestay.domain.model.NeighborhoodReview
import ca.uqac.inf865.truestay.domain.model.PropertyReview
import ca.uqac.inf865.truestay.domain.model.Review
import com.google.firebase.firestore.DocumentSnapshot

data class ReviewDto(
    val id: String = "",
    val rentalId: String = "",
    val propertyId: String = "",
    val tenantId: String = "",
    val propertyReview: PropertyReviewDto? = null,
    val buildingReview: BuildingReviewDto? = null,
    val neighborhoodReview: NeighborhoodReviewDto? = null,
    val createdAt: Long = 0L
)

data class PropertyReviewDto(
    val generalCondition: Int = 0,
    val comfort: Int = 0,
    val compliance: Int = 0,
    val valueForMoney: Int = 0,
    val overallRating: Float = 0f,
    val comment: String = "",
    val photos: List<String> = emptyList()
)

data class BuildingReviewDto(
    val maintenance: Int = 0,
    val neighborhood: Int = 0,
    val security: Int = 0,
    val services: Int = 0,
    val overallRating: Float = 0f,
    val comment: String = "",
    val photos: List<String> = emptyList()
)

data class NeighborhoodReviewDto(
    val transport: Int = 0,
    val amenities: Int = 0,
    val calm: Int = 0,
    val safety: Int = 0,
    val atmosphere: Int = 0,
    val overallRating: Float = 0f,
    val comment: String = "",
    val photos: List<String> = emptyList()
)

// DTO -> Domain Conversion
fun ReviewDto.toDomain(): Review {
    return Review(
        id = id,
        rentalId = rentalId,
        propertyId = propertyId,
        tenantId = tenantId,
        propertyReview = propertyReview?.toDomain(),
        buildingReview = buildingReview?.toDomain(),
        neighborhoodReview = neighborhoodReview?.toDomain(),
        createdAt = createdAt
    )
}

fun PropertyReviewDto.toDomain(): PropertyReview {
    return PropertyReview(
        generalCondition = generalCondition,
        comfort = comfort,
        compliance = compliance,
        valueForMoney = valueForMoney,
        overallRating = overallRating,
        comment = comment,
        photos = photos
    )
}

fun BuildingReviewDto.toDomain(): BuildingReview {
    return BuildingReview(
        maintenance = maintenance,
        neighborhood = neighborhood,
        security = security,
        services = services,
        overallRating = overallRating,
        comment = comment,
        photos = photos
    )
}

fun NeighborhoodReviewDto.toDomain(): NeighborhoodReview {
    return NeighborhoodReview(
        transport = transport,
        amenities = amenities,
        calm = calm,
        safety = safety,
        atmosphere = atmosphere,
        overallRating = overallRating,
        comment = comment,
        photos = photos
    )
}

// Domain -> DTO Conversion
fun Review.toDto(): ReviewDto {
    return ReviewDto(
        id = id,
        rentalId = rentalId,
        propertyId = propertyId,
        tenantId = tenantId,
        propertyReview = propertyReview?.toDto(),
        buildingReview = buildingReview?.toDto(),
        neighborhoodReview = neighborhoodReview?.toDto(),
        createdAt = createdAt
    )
}

fun PropertyReview.toDto(): PropertyReviewDto {
    return PropertyReviewDto(
        generalCondition = generalCondition,
        comfort = comfort,
        compliance = compliance,
        valueForMoney = valueForMoney,
        overallRating = calculateOverallRating(), // Auto calculated
        comment = comment,
        photos = photos
    )
}

fun BuildingReview.toDto(): BuildingReviewDto {
    return BuildingReviewDto(
        maintenance = maintenance,
        neighborhood = neighborhood,
        security = security,
        services = services,
        overallRating = calculateOverallRating(), // Auto calculated
        comment = comment,
        photos = photos
    )
}

fun NeighborhoodReview.toDto(): NeighborhoodReviewDto {
    return NeighborhoodReviewDto(
        transport = transport,
        amenities = amenities,
        calm = calm,
        safety = safety,
        atmosphere = atmosphere,
        overallRating = calculateOverallRating(), // Auto calculated
        comment = comment,
        photos = photos
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toReviewDto(): ReviewDto? {
    return try {
        toObject(ReviewDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}