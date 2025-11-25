package ca.uqac.inf865.truestay.presentation.tenant.review

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
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

    // UI State
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewFormViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
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
                                                comment = propReview.comment
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
                                                comment = buildReview.comment
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
                                                comment = neighReview.comment
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

    fun addPhotos(uris: List<Uri>) {
        uiState = uiState.copy(photoUris = uiState.photoUris + uris)
    }

    fun removePhoto(uri: Uri) {
        uiState = uiState.copy(photoUris = uiState.photoUris.filter { it != uri })
    }

    fun submitReview(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, errorMessage = null)
            // TODO: Implement actual review submission
            // For now, just simulate success
            kotlinx.coroutines.delay(500)
            uiState = uiState.copy(isSubmitting = false)
            onSuccess()
        }
    }
}