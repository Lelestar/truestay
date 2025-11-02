package ca.uqac.inf865.truestay.domain.model

data class Review(
    val id: String = "",
    val rentalId: String = "",
    val propertyId: String = "", // Denormalized for easier access
    val tenantId: String = "",
    val propertyReview: PropertyReview? = null,
    val buildingReview: BuildingReview? = null,
    val neighborhoodReview: NeighborhoodReview? = null,
    val createdAt: Long = 0L
)

data class PropertyReview(
    val generalCondition: Int = 0,
    val comfort: Int = 0,
    val compliance: Int = 0,
    val valueForMoney: Int = 0,
    val overallRating: Float = 0f, // Calculated
    val comment: String = "",
    val photos: List<String> = emptyList()
) {
    fun calculateOverallRating(): Float {
        return (generalCondition + comfort + compliance + valueForMoney) / 4f
    }
}

data class BuildingReview(
    val maintenance: Int = 0,
    val neighborhood: Int = 0,
    val security: Int = 0,
    val services: Int = 0,
    val overallRating: Float = 0f, // Calculated
    val comment: String = "",
    val photos: List<String> = emptyList()
) {
    fun calculateOverallRating(): Float {
        return (maintenance + neighborhood + security + services) / 4f
    }
}

data class NeighborhoodReview(
    val transport: Int = 0,
    val amenities: Int = 0,
    val calm: Int = 0,
    val safety: Int = 0,
    val atmosphere: Int = 0,
    val overallRating: Float = 0f, // Calculated
    val comment: String = "",
    val photos: List<String> = emptyList()
) {
    fun calculateOverallRating(): Float {
        return (transport + amenities + calm + safety + atmosphere) / 5f
    }
}

enum class ReviewType {
    PROPERTY,
    BUILDING,
    NEIGHBORHOOD
}