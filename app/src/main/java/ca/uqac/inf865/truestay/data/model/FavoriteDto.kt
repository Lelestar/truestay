package ca.uqac.inf865.truestay.data.model

import ca.uqac.inf865.truestay.domain.model.Favorite
import com.google.firebase.firestore.DocumentSnapshot

data class FavoriteDto(
    val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val addedAt: Long = 0L
)

// DTO -> Domain Conversion
fun FavoriteDto.toDomain(): Favorite {
    return Favorite(
        id = id,
        userId = userId,
        propertyId = propertyId,
        addedAt = addedAt
    )
}

// Domain -> DTO Conversion
fun Favorite.toDto(): FavoriteDto {
    return FavoriteDto(
        id = id,
        userId = userId,
        propertyId = propertyId,
        addedAt = addedAt
    )
}

// Firestore Document -> DTO Conversion
fun DocumentSnapshot.toFavoriteDto(): FavoriteDto? {
    return try {
        toObject(FavoriteDto::class.java)?.copy(id = id)
    } catch (e: Exception) {
        null
    }
}