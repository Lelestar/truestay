package ca.uqac.inf865.truestay.domain.usecase.review

import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val propertyRepository: PropertyRepository
) {
    /**
     * Submits a review (addition or update) and automatically recalculates
     * the property ratings.
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

            // 4. Recalculate and update property ratings (non-blocking)
            // If this fails, the review is still saved successfully
            try {
                updatePropertyRatings(review.propertyId)
            } catch (e: Exception) {
                // Silently ignore property rating update failures
                // Ratings will be calculated on-the-fly when loading properties
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
                propertyReview = new.propertyReview,
                createdAt = existing.createdAt // Keep original creation date
            )
            ReviewType.BUILDING -> existing.copy(
                buildingReview = new.buildingReview,
                createdAt = existing.createdAt
            )
            ReviewType.NEIGHBORHOOD -> existing.copy(
                neighborhoodReview = new.neighborhoodReview,
                createdAt = existing.createdAt
            )
        }
    }

    /**
     * Recalculates and updates property ratings
     */
    private suspend fun updatePropertyRatings(propertyId: String) {
        // 1. Get all reviews for the property
        val allReviews = reviewRepository.getReviewsByProperty(propertyId).getOrThrow()

        // 2. Calculate new ratings
        val newRatings = calculatePropertyRatings(allReviews)

        // 3. Update property with new ratings
        val property = propertyRepository.getPropertyById(propertyId).getOrThrow()
        val updatedProperty = property.copy(
            ratings = newRatings,
            updatedAt = System.currentTimeMillis()
        )
        propertyRepository.updateProperty(updatedProperty).getOrThrow()
    }

    /**
     * Calculates average ratings for all categories
     */
    private fun calculatePropertyRatings(reviews: List<Review>): PropertyRatings {
        if (reviews.isEmpty()) return PropertyRatings()

        val propertyReviews = reviews.mapNotNull { it.propertyReview }
        val buildingReviews = reviews.mapNotNull { it.buildingReview }
        val neighborhoodReviews = reviews.mapNotNull { it.neighborhoodReview }

        return PropertyRatings(
            // Property ratings
            propertyAverageRating = if (propertyReviews.isNotEmpty()) {
                propertyReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            propertyReviewCount = propertyReviews.size,

            // Building ratings
            buildingAverageRating = if (buildingReviews.isNotEmpty()) {
                buildingReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            buildingReviewCount = buildingReviews.size,

            // Neighborhood ratings
            neighborhoodAverageRating = if (neighborhoodReviews.isNotEmpty()) {
                neighborhoodReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            neighborhoodReviewCount = neighborhoodReviews.size
        )
    }
}