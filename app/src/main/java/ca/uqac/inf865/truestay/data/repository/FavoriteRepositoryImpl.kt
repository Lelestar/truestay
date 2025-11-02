package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.FavoriteDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Favorite
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : FavoriteRepository {

    override suspend fun addFavorite(userId: String, propertyId: String): Result<String> {
        return try {
            val favorite = FavoriteDto(
                userId = userId,
                propertyId = propertyId,
                addedAt = System.currentTimeMillis()
            )
            val id = firestoreDataSource.addDocument("favorites", favorite)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(userId: String, propertyId: String): Result<Unit> {
        return try {
            val favorites = firestoreDataSource.queryDocuments(
                "favorites",
                "userId",
                userId,
                FavoriteDto::class.java
            )

            val favorite = favorites.find { it.propertyId == propertyId }
                ?: return Result.failure(Exception("Favorite not found"))

            firestoreDataSource.deleteDocument("favorites", favorite.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavorites(userId: String): Result<List<Favorite>> {
        return try {
            val favorites = firestoreDataSource.queryDocuments(
                "favorites",
                "userId",
                userId,
                FavoriteDto::class.java
            ).map { it.toDomain() }
            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFavorite(userId: String, propertyId: String): Result<Boolean> {
        return try {
            val favorites = firestoreDataSource.queryDocuments(
                "favorites",
                "userId",
                userId,
                FavoriteDto::class.java
            )
            val isFavorite = favorites.any { it.propertyId == propertyId }
            Result.success(isFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}