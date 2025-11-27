package ca.uqac.inf865.truestay.domain.usecase.review

import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    /**
     * Submits a review (addition or update).
     * Property ratings are automatically updated via Cloud Functions.
     *
     * @param review The review to be submitted (empty id = addition, existing id = update)
     * @param reviewType The type of review (“property,” “building,” “neighborhood”)
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(review: Review, reviewType: ReviewType): Result<Unit> {
        return try {
            // 1. Get existing review if any
            val existingReview = if (review.rentalId.isNotEmpty()) {
                getExistingReview(review.rentalId, review.propertyId)
            } else null

            // 2. Create or update the review object
            val reviewToSubmit = if (existingReview != null) {
                // Update : merge with existing
                mergeReviews(existingReview, review, reviewType)
            } else {
                // New review
                review
            }

            // 3. Save review (add ou update)
            if (existingReview != null) {
                reviewRepository.updateReview(reviewToSubmit).getOrThrow()
            } else {
                reviewRepository.addReview(reviewToSubmit).getOrThrow()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get existing review for a given rental
     */
    private suspend fun getExistingReview(rentalId: String, propertyId: String): Review? {
        return try {
            val reviews = reviewRepository.getReviewsByProperty(propertyId).getOrThrow()
            reviews.find { it.rentalId == rentalId }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Merge new review with existing one based on review type
     */
    private fun mergeReviews(existing: Review, new: Review, reviewType: ReviewType): Review {
        return when (reviewType) {
            ReviewType.PROPERTY -> existing.copy(
                id = existing.id, // Keep existing ID
                propertyReview = new.propertyReview,
                createdAt = existing.createdAt // Keep original creation date
            )
            ReviewType.BUILDING -> existing.copy(
                id = existing.id, // Keep existing ID
                buildingReview = new.buildingReview,
                createdAt = existing.createdAt
            )
            ReviewType.NEIGHBORHOOD -> existing.copy(
                id = existing.id, // Keep existing ID
                neighborhoodReview = new.neighborhoodReview,
                createdAt = existing.createdAt
            )
        }
    }
}
