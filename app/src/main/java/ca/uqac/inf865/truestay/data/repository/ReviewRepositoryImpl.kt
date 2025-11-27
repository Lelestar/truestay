package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.ReviewDto
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : ReviewRepository {

    override suspend fun addReview(review: Review): Result<String> {
        return try {
            // Generate a document reference to get the ID first
            val docRef = firestoreDataSource.reviewsCollection().document()
            val documentId = docRef.id

            // Create DTO with the generated ID
            val reviewDto = review.toDto().copy(
                id = documentId,
                createdAt = System.currentTimeMillis()
            )

            // Save to Firestore
            docRef.set(reviewDto).await()

            Result.success(documentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReview(review: Review): Result<Unit> {  // ✨ NOUVEAU
        return try {
            firestoreDataSource.updateDocument("reviews", review.id, review.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReviewsByProperty(propertyId: String): Result<List<Review>> {
        return try {
            val reviews = firestoreDataSource.queryDocuments(
                "reviews",
                "propertyId",
                propertyId,
                ReviewDto::class.java
            ).map { it.toDomain() }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReviewById(id: String): Result<Review> {
        return try {
            val review = firestoreDataSource.getDocument("reviews", id, ReviewDto::class.java)
                ?: return Result.failure(Exception("Review not found"))
            Result.success(review.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReviewByRental(rentalId: String): Result<Review?> {
        return try {
            val reviews = firestoreDataSource.queryDocuments(
                "reviews",
                "rentalId",
                rentalId,
                ReviewDto::class.java
            )
            // There should be at most one review per rental
            Result.success(reviews.firstOrNull()?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            firestoreDataSource.deleteDocument("reviews", reviewId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}