package ca.uqac.inf865.truestay.domain.usecase.review

import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

class DeleteReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val storageRepository: StorageRepository
) {
    /**
     * Deletes a specific section of a review.
     * Also deletes associated images from storage.
     * If all sections are removed, the entire review document is deleted.
     *
     * @param reviewId The ID of the review to modify.
     * @param reviewType The type of review section to remove (PROPERTY, BUILDING, NEIGHBORHOOD).
     * @return Result<Unit> indicating success or failure.
     */
    suspend operator fun invoke(reviewId: String, reviewType: ReviewType): Result<Unit> {
        return try {
            val reviewResult = reviewRepository.getReviewById(reviewId)
            val review = reviewResult.getOrNull() ?: return Result.failure(Exception("Review not found"))

            // Identify photos to delete based on the section being removed
            val photosToDelete = when (reviewType) {
                ReviewType.PROPERTY -> review.propertyReview?.photos
                ReviewType.BUILDING -> review.buildingReview?.photos
                ReviewType.NEIGHBORHOOD -> review.neighborhoodReview?.photos
            } ?: emptyList()

            // Delete photos from storage if any exist
            if (photosToDelete.isNotEmpty()) {
                try {
                    storageRepository.deleteImages(photosToDelete)
                } catch (e: Exception) {
                    // Log error but continue with review deletion
                    e.printStackTrace()
                }
            }

            val updatedReview = when (reviewType) {
                ReviewType.PROPERTY -> review.copy(propertyReview = null)
                ReviewType.BUILDING -> review.copy(buildingReview = null)
                ReviewType.NEIGHBORHOOD -> review.copy(neighborhoodReview = null)
            }

            // Check if the review is completely empty
            if (updatedReview.propertyReview == null &&
                updatedReview.buildingReview == null &&
                updatedReview.neighborhoodReview == null
            ) {
                // Delete the entire review document
                reviewRepository.deleteReview(reviewId)
            } else {
                // Update the review document
                reviewRepository.updateReview(updatedReview)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
