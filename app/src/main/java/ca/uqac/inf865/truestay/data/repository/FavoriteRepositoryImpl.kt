package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.FavoriteDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Favorite
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : FavoriteRepository {

    override suspend fun addFavorite(userId: String, propertyId: String): Result<String> {
        return try {
            println("FavoriteRepository: Adding favorite - userId: $userId, propertyId: $propertyId")
            // Generate a document reference to get the ID first
            val docRef = firestoreDataSource.favoritesCollection().document()
            val documentId = docRef.id

            println("FavoriteRepository: Generated documentId: $documentId")

            // Create a map with the document ID included
            val favoriteData = mapOf(
                "id" to documentId,
                "userId" to userId,
                "propertyId" to propertyId,
                "addedAt" to System.currentTimeMillis()
            )

            // Set the document with the data
            docRef.set(favoriteData).await()
            println("FavoriteRepository: Successfully added favorite")
            Result.success(documentId)
        } catch (e: Exception) {
            println("FavoriteRepository: Error adding favorite: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(userId: String, propertyId: String): Result<Unit> {
        return try {
            val favoritesWithIds = firestoreDataSource.queryDocumentsWithIds(
                "favorites",
                "userId",
                userId,
                FavoriteDto::class.java
            )

            val favoriteEntry = favoritesWithIds.find { (_, favorite) ->
                favorite.propertyId == propertyId
            } ?: return Result.failure(Exception("Favorite not found"))

            val (documentId, _) = favoriteEntry
            firestoreDataSource.deleteDocument("favorites", documentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavorites(userId: String): Result<List<Favorite>> {
        return try {
            println("FavoriteRepository: Getting favorites for userId: $userId")
            val favoritesWithIds = firestoreDataSource.queryDocumentsWithIds(
                "favorites",
                "userId",
                userId,
                FavoriteDto::class.java
            )

            println("FavoriteRepository: Found ${favoritesWithIds.size} favorites")

            val favorites = favoritesWithIds.map { (documentId, favoriteDto) ->
                println("FavoriteRepository: Favorite - documentId: $documentId, propertyId: ${favoriteDto.propertyId}")
                // Create a new FavoriteDto with the correct document ID
                FavoriteDto(
                    id = documentId,
                    userId = favoriteDto.userId,
                    propertyId = favoriteDto.propertyId,
                    addedAt = favoriteDto.addedAt
                ).toDomain()
            }
            Result.success(favorites)
        } catch (e: Exception) {
            println("FavoriteRepository: Error getting favorites: ${e.message}")
            e.printStackTrace()
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