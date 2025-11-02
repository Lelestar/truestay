package ca.uqac.inf865.truestay.domain.model

data class Favorite(
    val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val addedAt: Long = 0L
)