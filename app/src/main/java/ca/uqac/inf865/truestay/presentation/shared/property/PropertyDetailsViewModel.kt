package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Available filters for the reviews section (logement / immeuble / quartier).
 */
enum class ReviewFilterType {
    PROPERTY,
    BUILDING,
    NEIGHBORHOOD
}

/**
 * Combines a review with its author for display.
 */
data class ReviewWithUser(
    val review: Review,
    val user: User?
)

/**
 * UI state for the PropertyDetails screen.
 */
data class PropertyDetailsUiState(
    val property: Property? = null,
    val rental: Rental? = null,
    val reviews: List<ReviewWithUser> = emptyList(),
    val landlord: User? = null,
    val currentUser: User? = null,
    val isFavorite: Boolean = false,
    val selectedReviewFilter: ReviewFilterType = ReviewFilterType.PROPERTY,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteMessageRes: Int? = null,
    val hasPendingRental: Boolean = false
)


@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val rentalRepository: RentalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val propertyId: String = savedStateHandle.get<String>("propertyId") ?: ""

    var uiState by mutableStateOf(PropertyDetailsUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        loadCurrentUser()
        loadPropertyDetails()
        checkIfFavorite()
        checkPendingRental()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    uiState = uiState.copy(currentUser = user)
                }
        }
    }

    private fun loadPropertyDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // Load property
            propertyRepository.getPropertyById(propertyId)
                .onSuccess { property ->
                    uiState = uiState.copy(
                        property = property,
                        isLoading = false,
                        error = null
                    )
                    loadReviewIfExists(property.id)
                    if (property.landlordId.isNotBlank()) {
                        loadLandlord(property.landlordId)
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun loadLandlord(landlordId: String) {
        viewModelScope.launch {
            userRepository.getUserById(landlordId)
                .onSuccess { landlord ->
                    uiState = uiState.copy(landlord = landlord)
                }
        }
    }

    private fun loadReviewIfExists(propertyId: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsByProperty(propertyId)
                .onSuccess { reviews ->
                    // Load users for each review
                    val reviewsWithUsers = reviews.map { review ->
                        val user = userRepository.getUserById(review.tenantId).getOrNull()
                        ReviewWithUser(review, user)
                    }

                    uiState = uiState.copy(
                        reviews = reviewsWithUsers
                    )
                }
        }
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    val userId = user?.id ?: return@onSuccess
                    favoriteRepository.isFavorite(userId, propertyId)
                        .onSuccess { isFavorite ->
                            uiState = uiState.copy(isFavorite = isFavorite)
                        }
                }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    val userId = user?.id ?: return@onSuccess
                    val currentState = uiState.isFavorite

                    if (currentState) {
                        // Remove from favorites
                        favoriteRepository.removeFavorite(userId, propertyId)
                            .onSuccess {
                                uiState = uiState.copy(
                                    isFavorite = false,
                                    favoriteMessageRes = ca.uqac.inf865.truestay.R.string.property_details_favorite_removed
                                )
                            }
                            .onFailure {
                                uiState = uiState.copy(
                                    favoriteMessageRes = ca.uqac.inf865.truestay.R.string.property_details_favorite_error
                                )
                            }
                    } else {
                        // Add to favorites
                        favoriteRepository.addFavorite(userId, propertyId)
                            .onSuccess {
                                uiState = uiState.copy(
                                    isFavorite = true,
                                    favoriteMessageRes = ca.uqac.inf865.truestay.R.string.property_details_favorite_added
                                )
                            }
                            .onFailure {
                                uiState = uiState.copy(
                                    favoriteMessageRes = ca.uqac.inf865.truestay.R.string.property_details_favorite_error
                                )
                            }
                    }
                }
        }
    }

    fun clearFavoriteMessage() {
        uiState = uiState.copy(favoriteMessageRes = null)
    }

    fun setReviewFilter(filterType: ReviewFilterType) {
        uiState = uiState.copy(selectedReviewFilter = filterType)
    }

    private fun checkPendingRental() {
        viewModelScope.launch {
            rentalRepository.getRentalsByProperty(propertyId)
                .onSuccess { rentals ->
                    val hasPending = rentals.any {
                        it.status == ca.uqac.inf865.truestay.domain.model.RentalStatus.PENDING
                    }
                    uiState = uiState.copy(hasPendingRental = hasPending)
                }
                .onFailure {
                }
        }
    }

    fun deleteProperty(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val property = uiState.property ?: return@launch
            val currentUser = uiState.currentUser

            // Security check: Only allow deletion if the property belongs to the current user
            if (currentUser == null || property.landlordId != currentUser.id) {
                uiState = uiState.copy(error = "Unauthorized action")
                return@launch
            }

            if (!property.isAvailable) {
                uiState = uiState.copy(error = "Unauthorized action")
                return@launch
            }

            propertyRepository.deleteProperty(property.id)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    uiState = uiState.copy(error = it.message)
                }
        }
    }

    fun togglePropertyStatus(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val property = uiState.property ?: return@launch
            val currentUser = uiState.currentUser

            // Security check: Only allow status change if the property belongs to the current user
            if (currentUser == null || property.landlordId != currentUser.id) {
                uiState = uiState.copy(error = "Unauthorized action")
                return@launch
            }

            if (!property.isAvailable && property.status == PropertyStatus.PUBLISHED) {
                uiState = uiState.copy(error = "Unauthorized action")
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
                    uiState = uiState.copy(property = updatedProperty)
                    onSuccess()
                }
                .onFailure {
                    uiState = uiState.copy(error = it.message)
                }
        }
    }
}
