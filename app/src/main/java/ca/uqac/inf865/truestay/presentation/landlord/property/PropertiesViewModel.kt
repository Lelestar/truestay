package ca.uqac.inf865.truestay.presentation.landlord.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Types of errors that may occur in the properties screen
 */
enum class PropertiesError {
    NOT_AUTHENTICATED,
    LOAD_FAILED
}

/**
 * UI state for the properties screen
 */
data class PropertiesUiState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val error: PropertiesError? = null,
    val currentUserId: String? = null
)

/**
 * ViewModel for managing the landlord's properties screen
 *
 * Manages the loading and display of properties owned by the landlord.
 */
@HiltViewModel
class PropertiesViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository,
    private val reviewRepository: ca.uqac.inf865.truestay.domain.repository.ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PropertiesUiState(isLoading = true))
    val uiState: StateFlow<PropertiesUiState> = _uiState.asStateFlow()

    init {
        loadProperties()
    }

    /**
     * Loads the properties for the current landlord
     */
    fun loadProperties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                properties = emptyList(),
                                error = PropertiesError.NOT_AUTHENTICATED,
                                currentUserId = null
                            )
                        }
                        return@onSuccess
                    }
                    fetchPropertiesForLandlord(user.id)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            properties = emptyList(),
                            error = PropertiesError.LOAD_FAILED,
                            currentUserId = null
                        )
                    }
                }
        }
    }

    /**
     * Retrieves properties for a specific landlord
     */
    private suspend fun fetchPropertiesForLandlord(landlordId: String) {
        propertyRepository.getPropertiesByLandlord(landlordId)
            .onSuccess { properties ->
                // Enrich properties with calculated ratings
                val enrichedProperties = enrichPropertiesWithRatings(properties)

                _uiState.update {
                    it.copy(
                        properties = enrichedProperties,
                        isLoading = false,
                        error = null,
                        currentUserId = landlordId
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = PropertiesError.LOAD_FAILED
                    )
                }
            }
    }

    private suspend fun enrichPropertiesWithRatings(properties: List<Property>): List<Property> {
        return properties.map { property ->
            try {
                val reviews = reviewRepository.getReviewsByProperty(property.id).getOrNull() ?: emptyList()
                val calculatedRatings = calculateRatingsFromReviews(reviews)
                property.copy(ratings = calculatedRatings)
            } catch (e: Exception) {
                property
            }
        }
    }

    private fun calculateRatingsFromReviews(reviews: List<ca.uqac.inf865.truestay.domain.model.Review>): ca.uqac.inf865.truestay.domain.model.PropertyRatings {
        if (reviews.isEmpty()) return ca.uqac.inf865.truestay.domain.model.PropertyRatings()

        val propertyReviews = reviews.mapNotNull { it.propertyReview }
        val buildingReviews = reviews.mapNotNull { it.buildingReview }
        val neighborhoodReviews = reviews.mapNotNull { it.neighborhoodReview }

        return ca.uqac.inf865.truestay.domain.model.PropertyRatings(
            propertyAverageRating = if (propertyReviews.isNotEmpty()) {
                propertyReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            propertyReviewCount = propertyReviews.size,
            buildingAverageRating = if (buildingReviews.isNotEmpty()) {
                buildingReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            buildingReviewCount = buildingReviews.size,
            neighborhoodAverageRating = if (neighborhoodReviews.isNotEmpty()) {
                neighborhoodReviews.map { it.overallRating }.average().toFloat()
            } else 0f,
            neighborhoodReviewCount = neighborhoodReviews.size
        )
    }

    /**
     * Deletes a property if it belongs to the current user
     */
    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            val currentUserId = _uiState.value.currentUserId
            val property = _uiState.value.properties.find { it.id == propertyId }

            // Security check: Only allow deletion if the property belongs to the current user
            if (currentUserId == null || property == null || property.landlordId != currentUserId) {
                _uiState.update {
                    it.copy(error = PropertiesError.LOAD_FAILED)
                }
                return@launch
            }

            if (!property.isAvailable) {
                _uiState.update {
                    it.copy(error = PropertiesError.LOAD_FAILED)
                }
                return@launch
            }

            propertyRepository.deleteProperty(propertyId)
                .onSuccess {
                    // Refresh the list after deletion
                    loadProperties()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(error = PropertiesError.LOAD_FAILED)
                    }
                }
        }
    }

    /**
     * Toggles the status of a property between PUBLISHED and PAUSED if it belongs to the current user
     */
    fun togglePropertyStatus(propertyId: String) {
        viewModelScope.launch {
            val currentUserId = _uiState.value.currentUserId
            val property = _uiState.value.properties.find { it.id == propertyId }

            // Security check: Only allow status change if the property belongs to the current user
            if (currentUserId == null || property == null || property.landlordId != currentUserId) {
                _uiState.update {
                    it.copy(error = PropertiesError.LOAD_FAILED)
                }
                return@launch
            }

            if (!property.isAvailable && property.status == PropertyStatus.PUBLISHED) {
                _uiState.update {
                    it.copy(error = PropertiesError.LOAD_FAILED)
                }
                return@launch
            }

            val newStatus = if (property.status == PropertyStatus.PUBLISHED) {
                PropertyStatus.PAUSED
            } else {
                PropertyStatus.PUBLISHED
            }

            val updatedProperty = property.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )

            propertyRepository.updateProperty(updatedProperty)
                .onSuccess {
                    // Refresh the list after update
                    loadProperties()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(error = PropertiesError.LOAD_FAILED)
                    }
                }
        }
    }
}
