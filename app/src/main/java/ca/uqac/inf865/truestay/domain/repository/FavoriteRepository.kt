package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Favorite

interface FavoriteRepository {
    suspend fun addFavorite(userId: String, propertyId: String): Result<String>
    suspend fun removeFavorite(userId: String, propertyId: String): Result<Unit>
    suspend fun getFavorites(userId: String): Result<List<Favorite>>
    suspend fun isFavorite(userId: String, propertyId: String): Result<Boolean>
}