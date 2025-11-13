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
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val favoriteRepository: FavoriteRepository,
    private val rentalRepository: RentalRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val propertyId: String = savedStateHandle.get<String>("propertyId") ?: ""

    var uiState by mutableStateOf(PropertyDetailsUiState())
        private set

    init {
        loadPropertyDetails()
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
                    // Prendre le premier avis de l'utilisateur actuel si disponible
                    uiState = uiState.copy(
                        userReview = reviews.firstOrNull()
                    )
                }
        }
    }
}

data class PropertyDetailsUiState(
    val property: Property? = null,
    val rental: Rental? = null,
    val userReview: Review? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
