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
    val photoUris: List<Uri> = emptyList(),
    val uploadedPhotoUrls: List<String> = emptyList(), // Photos déjà uploadées sur le serveur

    // UI State
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val errorMessage: String? = null,
    val photoUploadError: String? = null
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

    init {
        loadExistingReview()
    }

    private fun loadExistingReview() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

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
                                                uploadedPhotoUrls = propReview.photos
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
                                                uploadedPhotoUrls = buildReview.photos
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
                                                uploadedPhotoUrls = neighReview.photos
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // En cas d'erreur, on laisse le formulaire vide
            }

            uiState = uiState.copy(isLoading = false)
        }
    }

    // Property review setters
    fun setGeneralCondition(rating: Int) {
        uiState = uiState.copy(generalCondition = rating)
    }

    fun setComfort(rating: Int) {
        uiState = uiState.copy(comfort = rating)
    }

    fun setCompliance(rating: Int) {
        uiState = uiState.copy(compliance = rating)
    }

    fun setValueForMoney(rating: Int) {
        uiState = uiState.copy(valueForMoney = rating)
    }

    // Building review setters
    fun setMaintenance(rating: Int) {
        uiState = uiState.copy(maintenance = rating)
    }

    fun setNeighborhood(rating: Int) {
        uiState = uiState.copy(neighborhood = rating)
    }

    fun setSecurity(rating: Int) {
        uiState = uiState.copy(security = rating)
    }

    fun setServices(rating: Int) {
        uiState = uiState.copy(services = rating)
    }

    // Neighborhood review setters
    fun setTransport(rating: Int) {
        uiState = uiState.copy(transport = rating)
    }

    fun setAmenities(rating: Int) {
        uiState = uiState.copy(amenities = rating)
    }

    fun setCalm(rating: Int) {
        uiState = uiState.copy(calm = rating)
    }

    fun setSafety(rating: Int) {
        uiState = uiState.copy(safety = rating)
    }

    fun setAtmosphere(rating: Int) {
        uiState = uiState.copy(atmosphere = rating)
    }

    // Common setters
    fun setComment(comment: String) {
        uiState = uiState.copy(comment = comment)
    }

    fun addPhoto(uri: Uri) {
        val currentCount = uiState.uploadedPhotoUrls.size
        val maxPhotos = 5

        if (currentCount < maxPhotos) {
            uploadPhoto(uri)
        }
    }

    fun addPhotos(uris: List<Uri>) {
        val currentCount = uiState.uploadedPhotoUrls.size
        val maxPhotos = 5
        val availableSlots = maxPhotos - currentCount
        val photosToAdd = uris.take(availableSlots)

        photosToAdd.forEach { uri ->
            uploadPhoto(uri)
        }
    }

    private fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isUploadingPhoto = true, photoUploadError = null)

            try {
                // Upload image to storage
                val path = "reviews/$rentalId/${System.currentTimeMillis()}"
                val result = storageRepository.uploadImage(uri, path)

                result.onSuccess { url ->
                    val newPhotoUrls = uiState.uploadedPhotoUrls + url
                    uiState = uiState.copy(
                        uploadedPhotoUrls = newPhotoUrls,
                        isUploadingPhoto = false,
                        photoUploadError = null
                    )
                }.onFailure { exception ->
                    uiState = uiState.copy(
                        isUploadingPhoto = false,
                        photoUploadError = exception.message
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isUploadingPhoto = false,
                    photoUploadError = e.message
                )
            }
        }
    }

    fun removePhoto(photoUrl: String) {
        viewModelScope.launch {
            try {
                // Delete from storage
                storageRepository.deleteImage(photoUrl)

                // Remove from UI state
                uiState = uiState.copy(
                    uploadedPhotoUrls = uiState.uploadedPhotoUrls.filter { it != photoUrl }
                )
            } catch (e: Exception) {
                // Silently fail or show error
            }
        }
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
                            photos = uiState.uploadedPhotoUrls
                        )
                        Review(
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
                            photos = uiState.uploadedPhotoUrls
                        )
                        Review(
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
                            photos = uiState.uploadedPhotoUrls
                        )
                        Review(
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
                    errorMessage = e.message ?: "Une erreur est survenue lors de la soumission de l'avis"
                )
            }
        }
    }
}