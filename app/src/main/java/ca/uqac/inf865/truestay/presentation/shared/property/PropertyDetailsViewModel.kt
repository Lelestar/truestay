package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
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

enum class ReviewFilterType {
    PROPERTY,
    BUILDING,
    NEIGHBORHOOD
}

data class ReviewWithUser(
    val review: Review,
    val user: User?
)

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val favoriteRepository: FavoriteRepository,
    private val rentalRepository: RentalRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val propertyId: String = savedStateHandle.get<String>("propertyId") ?: ""

    var uiState by mutableStateOf(PropertyDetailsUiState())
        private set

    init {
        loadPropertyDetails()
        checkIfFavorite()
    }

    private fun loadPropertyDetails() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Load property
            propertyRepository.getPropertyById(propertyId)
                .onSuccess { property ->
                    uiState = uiState.copy(
                        property = property,
                        isLoading = false
                    )
                    loadRentalIfExists(property.id)
                    loadReviewIfExists(property.id)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun loadRentalIfExists(@Suppress("UNUSED_PARAMETER") propertyId: String) {
        // TODO: Implémenter la récupération de la location active pour cette propriété
        // Pour l'instant, on simule qu'il n'y a pas de location
    }

    private fun loadReviewIfExists(propertyId: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsByProperty(propertyId)
                .onSuccess { reviews ->
                    // Charger les infos utilisateur pour chaque review
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
                                uiState = uiState.copy(isFavorite = false)
                            }
                            .onFailure { error ->
                                // Log error but keep current state
                                println("Failed to remove favorite: ${error.message}")
                            }
                    } else {
                        // Add to favorites
                        favoriteRepository.addFavorite(userId, propertyId)
                            .onSuccess {
                                uiState = uiState.copy(isFavorite = true)
                            }
                            .onFailure { error ->
                                // Log error but keep current state
                                println("Failed to add favorite: ${error.message}")
                            }
                    }
                }
                .onFailure { error ->
                    println("Failed to get current user: ${error.message}")
                }
        }
    }

    fun setReviewFilter(filterType: ReviewFilterType) {
        uiState = uiState.copy(selectedReviewFilter = filterType)
    }
}

data class PropertyDetailsUiState(
    val property: Property? = null,
    val rental: Rental? = null,
    val reviews: List<ReviewWithUser> = emptyList(),
    val isFavorite: Boolean = false,
    val selectedReviewFilter: ReviewFilterType = ReviewFilterType.PROPERTY,
    val isLoading: Boolean = false,
    val error: String? = null
)
