package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.ReviewDto
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : ReviewRepository {

    override suspend fun addReview(review: Review): Result<String> {
        return try {
            val reviewDto = review.toDto().copy(createdAt = System.currentTimeMillis())
            val id = firestoreDataSource.addDocument("reviews", reviewDto)
            Result.success(id)
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
}