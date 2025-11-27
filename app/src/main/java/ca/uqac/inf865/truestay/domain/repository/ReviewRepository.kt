package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Review

interface ReviewRepository {
    suspend fun addReview(review: Review): Result<String>
    suspend fun updateReview(review: Review): Result<Unit>
    suspend fun getReviewsByProperty(propertyId: String): Result<List<Review>>
    suspend fun getReviewById(id: String): Result<Review>
    suspend fun getReviewByRental(rentalId: String): Result<Review?>
    suspend fun deleteReview(reviewId: String): Result<Unit>
}