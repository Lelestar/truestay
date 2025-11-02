package ca.uqac.inf865.truestay.domain.model

data class PropertyFilters(
    // Price
    val minPrice: Double? = null,
    val maxPrice: Double? = null,

    // Surface
    val minSurface: Double? = null,
    val maxSurface: Double? = null,

    // Number of bedrooms
    val bedroomCounts: List<BedroomCount> = emptyList(),

    // Minimum average rating
    val minRating: Float? = null,

    // Availability
    val availableOnly: Boolean = true,

    // Geographical area (for the map)
    val geoBounds: GeoBounds? = null,

    // Text search (name, description, address)
    val searchQuery: String? = null
)

enum class BedroomCount(val value: Int, val label: String) {
    ONE(1, "1"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR_PLUS(4, "4+");

    fun matches(actualCount: Int): Boolean {
        return when (this) {
            ONE -> actualCount == 1
            TWO -> actualCount == 2
            THREE -> actualCount == 3
            FOUR_PLUS -> actualCount >= 4
        }
    }
}

data class GeoBounds(
    val northEast: LatLng,
    val southWest: LatLng
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)