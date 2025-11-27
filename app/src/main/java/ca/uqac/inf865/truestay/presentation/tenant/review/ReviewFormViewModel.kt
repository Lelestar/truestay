package ca.uqac.inf865.truestay.presentation.tenant.review

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.BuildingReview
import ca.uqac.inf865.truestay.domain.model.NeighborhoodReview
import ca.uqac.inf865.truestay.domain.model.PropertyReview
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import ca.uqac.inf865.truestay.domain.usecase.review.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewFormUiState(
    // Property review ratings
    val generalCondition: Int = 0,
    val comfort: Int = 0,
    val compliance: Int = 0,
    val valueForMoney: Int = 0,

    // Building review ratings
    val maintenance: Int = 0,
    val neighborhood: Int = 0,
    val security: Int = 0,
    val services: Int = 0,

    // Neighborhood review ratings
    val transport: Int = 0,
    val amenities: Int = 0,
    val calm: Int = 0,
    val safety: Int = 0,
    val atmosphere: Int = 0,

    // Common fields
    val comment: String = "",
    val photoUris: List<Uri> = emptyList(), // Local URIs of new photos to upload
    val uploadedPhotoUrls: List<String> = emptyList(), // Already uploaded photos (for editing existing reviews)

    // UI State
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadError: String? = null, // Error while loading existing review
    val errorMessage: String? = null, // Error while submitting review
    val existingReviewId: String? = null, // ID of existing review (for updates)
    val hasChanges: Boolean = false // Track if any changes have been made since loading
)

@HiltViewModel
class ReviewFormViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val storageRepository: StorageRepository,
    private val rentalRepository: ca.uqac.inf865.truestay.domain.repository.RentalRepository,
    private val authRepository: ca.uqac.inf865.truestay.domain.repository.AuthRepository,
    private val submitReviewUseCase: SubmitReviewUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String = savedStateHandle.get<String>("rentalId") ?: ""
    private val reviewTypeString: String = savedStateHandle.get<String>("reviewType") ?: "PROPERTY"
    val reviewType: ReviewType = try {
        ReviewType.valueOf(reviewTypeString.uppercase())
    } catch (e: IllegalArgumentException) {
        ReviewType.PROPERTY
    }

    var uiState by mutableStateOf(ReviewFormUiState())
        private set

    private var initialState = ReviewFormUiState()

    init {
        loadExistingReview()
    }

    private fun checkChanges() {
        val isChanged = when (reviewType) {
            ReviewType.PROPERTY -> {
                uiState.generalCondition != initialState.generalCondition ||
                uiState.comfort != initialState.comfort ||
                uiState.compliance != initialState.compliance ||
                uiState.valueForMoney != initialState.valueForMoney ||
                uiState.comment != initialState.comment ||
                uiState.uploadedPhotoUrls != initialState.uploadedPhotoUrls ||
                uiState.photoUris.isNotEmpty()
            }
            ReviewType.BUILDING -> {
                uiState.maintenance != initialState.maintenance ||
                uiState.neighborhood != initialState.neighborhood ||
                uiState.security != initialState.security ||
                uiState.services != initialState.services ||
                uiState.comment != initialState.comment ||
                uiState.uploadedPhotoUrls != initialState.uploadedPhotoUrls ||
                uiState.photoUris.isNotEmpty()
            }
            ReviewType.NEIGHBORHOOD -> {
                uiState.transport != initialState.transport ||
                uiState.amenities != initialState.amenities ||
                uiState.calm != initialState.calm ||
                uiState.safety != initialState.safety ||
                uiState.atmosphere != initialState.atmosphere ||
                uiState.comment != initialState.comment ||
                uiState.uploadedPhotoUrls != initialState.uploadedPhotoUrls ||
                uiState.photoUris.isNotEmpty()
            }
        }
        uiState = uiState.copy(hasChanges = isChanged)
    }

    private fun loadExistingReview() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, loadError = null)

            try {
                // Get current user
                val currentUserResult = authRepository.getCurrentUser()
                val currentUser = currentUserResult.getOrNull()

                if (currentUser != null && rentalId.isNotEmpty()) {
                    // First, get the rental to obtain the propertyId
                    val rentalResult = rentalRepository.getRentalById(rentalId)
                    rentalResult.onSuccess { rental ->
                        // Then get reviews for this property
                        val reviewsResult = reviewRepository.getReviewsByProperty(rental.propertyId)

                        reviewsResult.onSuccess { reviews ->
                            // Find existing review for this tenant and rental
                            val existingReview = reviews.firstOrNull { review ->
                                review.tenantId == currentUser.id && review.rentalId == rentalId
                            }

                            existingReview?.let { review ->
                                when (reviewType) {
                                    ReviewType.PROPERTY -> {
                                        review.propertyReview?.let { propReview ->
                                            uiState = uiState.copy(
                                                generalCondition = propReview.generalCondition,
                                                comfort = propReview.comfort,
                                                compliance = propReview.compliance,
                                                valueForMoney = propReview.valueForMoney,
                                                comment = propReview.comment,
                                                uploadedPhotoUrls = propReview.photos,
                                                existingReviewId = review.id,
                                                hasChanges = false
                                            )
                                        }
                                    }
                                    ReviewType.BUILDING -> {
                                        review.buildingReview?.let { buildReview ->
                                            uiState = uiState.copy(
                                                maintenance = buildReview.maintenance,
                                                neighborhood = buildReview.neighborhood,
                                                security = buildReview.security,
                                                services = buildReview.services,
                                                comment = buildReview.comment,
                                                uploadedPhotoUrls = buildReview.photos,
                                                existingReviewId = review.id,
                                                hasChanges = false
                                            )
                                        }
                                    }
                                    ReviewType.NEIGHBORHOOD -> {
                                        review.neighborhoodReview?.let { neighReview ->
                                            uiState = uiState.copy(
                                                transport = neighReview.transport,
                                                amenities = neighReview.amenities,
                                                calm = neighReview.calm,
                                                safety = neighReview.safety,
                                                atmosphere = neighReview.atmosphere,
                                                comment = neighReview.comment,
                                                uploadedPhotoUrls = neighReview.photos,
                                                existingReviewId = review.id,
                                                hasChanges = false
                                            )
                                        }
                                    }
                                }
                                // Capture initial state for comparison
                                initialState = uiState
                            }
                        }
                    }
                }
                uiState = uiState.copy(isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    loadError = "error_load" // Will be resolved to string in UI
                )
            }
        }
    }

    /**
     * Retry loading the existing review after an error
     */
    fun retryLoadReview() {
        loadExistingReview()
    }

    // Property review setters
    fun setGeneralCondition(rating: Int) {
        uiState = uiState.copy(generalCondition = rating)
        checkChanges()
    }

    fun setComfort(rating: Int) {
        uiState = uiState.copy(comfort = rating)
        checkChanges()
    }

    fun setCompliance(rating: Int) {
        uiState = uiState.copy(compliance = rating)
        checkChanges()
    }

    fun setValueForMoney(rating: Int) {
        uiState = uiState.copy(valueForMoney = rating)
        checkChanges()
    }

    // Building review setters
    fun setMaintenance(rating: Int) {
        uiState = uiState.copy(maintenance = rating)
        checkChanges()
    }

    fun setNeighborhood(rating: Int) {
        uiState = uiState.copy(neighborhood = rating)
        checkChanges()
    }

    fun setSecurity(rating: Int) {
        uiState = uiState.copy(security = rating)
        checkChanges()
    }

    fun setServices(rating: Int) {
        uiState = uiState.copy(services = rating)
        checkChanges()
    }

    // Neighborhood review setters
    fun setTransport(rating: Int) {
        uiState = uiState.copy(transport = rating)
        checkChanges()
    }

    fun setAmenities(rating: Int) {
        uiState = uiState.copy(amenities = rating)
        checkChanges()
    }

    fun setCalm(rating: Int) {
        uiState = uiState.copy(calm = rating)
        checkChanges()
    }

    fun setSafety(rating: Int) {
        uiState = uiState.copy(safety = rating)
        checkChanges()
    }

    fun setAtmosphere(rating: Int) {
        uiState = uiState.copy(atmosphere = rating)
        checkChanges()
    }

    // Common setters
    fun setComment(comment: String) {
        uiState = uiState.copy(comment = comment)
        checkChanges()
    }

    /**
     * Adds a new photo URI to be uploaded later when submitting the review
     */
    fun addPhoto(uri: Uri) {
        val maxPhotos = 5
        val totalPhotos = uiState.photoUris.size + uiState.uploadedPhotoUrls.size

        if (totalPhotos < maxPhotos) {
            uiState = uiState.copy(
                photoUris = uiState.photoUris + uri
            )
            checkChanges()
        }
    }

    /**
     * Adds multiple photo URIs to be uploaded later when submitting the review
     */
    fun addPhotos(uris: List<Uri>) {
        val maxPhotos = 5
        val totalPhotos = uiState.photoUris.size + uiState.uploadedPhotoUrls.size
        val availableSlots = maxPhotos - totalPhotos
        val photosToAdd = uris.take(availableSlots)

        uiState = uiState.copy(
            photoUris = uiState.photoUris + photosToAdd
        )
        checkChanges()
    }

    /**
     * Removes a local photo URI (not yet uploaded)
     */
    fun removeLocalPhoto(uri: Uri) {
        uiState = uiState.copy(
            photoUris = uiState.photoUris.filter { it != uri }
        )
        checkChanges()
    }

    /**
     * Removes an already uploaded photo from an existing review
     */
    fun removeUploadedPhoto(photoUrl: String) {
        viewModelScope.launch {
            try {
                // Delete from storage
                storageRepository.deleteImage(photoUrl)

                // Remove from UI state
                uiState = uiState.copy(
                    uploadedPhotoUrls = uiState.uploadedPhotoUrls.filter { it != photoUrl }
                )
                checkChanges()
            } catch (e: Exception) {
                // Silently fail - photo will remain orphaned in storage
            }
        }
    }

    /**
     * Uploads all pending photos to storage
     * Returns the list of uploaded photo URLs
     */
    private suspend fun uploadAllPhotos(): List<String> {
        val uploadedUrls = mutableListOf<String>()

        uiState.photoUris.forEach { uri ->
            try {
                val path = "reviews/$rentalId/${System.currentTimeMillis()}_${uploadedUrls.size}"
                val result = storageRepository.uploadImage(uri, path)

                result.onSuccess { url ->
                    uploadedUrls.add(url)
                }.onFailure { exception ->
                    throw exception
                }
            } catch (e: Exception) {
                // If any upload fails, throw to rollback the submission
                throw Exception("Failed to upload photo: ${e.message}", e)
            }
        }

        return uploadedUrls
    }

    fun submitReview(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, errorMessage = null)

            try {
                // Get current user
                val currentUser = authRepository.getCurrentUser().getOrThrow()
                    ?: throw IllegalStateException("Utilisateur non connecté")

                // Get rental to obtain propertyId
                val rental = rentalRepository.getRentalById(rentalId).getOrThrow()

                // Upload all pending photos first
                val newlyUploadedUrls = uploadAllPhotos()

                // Combine existing uploaded photos with newly uploaded ones
                val allPhotoUrls = uiState.uploadedPhotoUrls + newlyUploadedUrls

                // Create the appropriate review object based on type
                val review = when (reviewType) {
                    ReviewType.PROPERTY -> {
                        val propertyReview = PropertyReview(
                            generalCondition = uiState.generalCondition,
                            comfort = uiState.comfort,
                            compliance = uiState.compliance,
                            valueForMoney = uiState.valueForMoney,
                            overallRating = (uiState.generalCondition + uiState.comfort +
                                           uiState.compliance + uiState.valueForMoney) / 4f,
                            comment = uiState.comment,
                            photos = allPhotoUrls
                        )
                        Review(
                            id = uiState.existingReviewId ?: "",
                            rentalId = rentalId,
                            propertyId = rental.propertyId,
                            tenantId = currentUser.id,
                            propertyReview = propertyReview,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    ReviewType.BUILDING -> {
                        val buildingReview = BuildingReview(
                            maintenance = uiState.maintenance,
                            neighborhood = uiState.neighborhood,
                            security = uiState.security,
                            services = uiState.services,
                            overallRating = (uiState.maintenance + uiState.neighborhood +
                                           uiState.security + uiState.services) / 4f,
                            comment = uiState.comment,
                            photos = allPhotoUrls
                        )
                        Review(
                            id = uiState.existingReviewId ?: "",
                            rentalId = rentalId,
                            propertyId = rental.propertyId,
                            tenantId = currentUser.id,
                            buildingReview = buildingReview,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    ReviewType.NEIGHBORHOOD -> {
                        val neighborhoodReview = NeighborhoodReview(
                            transport = uiState.transport,
                            amenities = uiState.amenities,
                            calm = uiState.calm,
                            safety = uiState.safety,
                            atmosphere = uiState.atmosphere,
                            overallRating = (uiState.transport + uiState.amenities + uiState.calm +
                                           uiState.safety + uiState.atmosphere) / 5f,
                            comment = uiState.comment,
                            photos = allPhotoUrls
                        )
                        Review(
                            id = uiState.existingReviewId ?: "",
                            rentalId = rentalId,
                            propertyId = rental.propertyId,
                            tenantId = currentUser.id,
                            neighborhoodReview = neighborhoodReview,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                }

                // Submit the review using the use case
                submitReviewUseCase(review, reviewType).getOrThrow()

                uiState = uiState.copy(isSubmitting = false, errorMessage = null)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    errorMessage = "error_submit" // Will be resolved to string in UI
                )
            }
        }
    }
}