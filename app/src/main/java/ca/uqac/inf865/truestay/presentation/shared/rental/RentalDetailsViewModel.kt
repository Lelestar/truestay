package ca.uqac.inf865.truestay.presentation.shared.rental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.domain.usecase.review.DeleteReviewUseCase

data class RentalDetailsUiState(
    val rental: Rental? = null,
    val property: Property? = null,
    val review: Review? = null,
    val tenant: User? = null,
    val landlord: User? = null,
    val currentUser: User? = null,
    val entryInventory: Inventory? = null,
    val exitInventory: Inventory? = null,
    val isLoading: Boolean = false,
    val isDeletingReview: Boolean = false,
    val errorRes: Int? = null,
    val deletionErrorMessageRes: Int? = null
)

@HiltViewModel
class RentalDetailsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val inventoryRepository: InventoryRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository,
    private val deleteReviewUseCase: DeleteReviewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalDetailsUiState(isLoading = true))
    val uiState: StateFlow<RentalDetailsUiState> = _uiState.asStateFlow()

    fun loadRental(rentalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorRes = null) }

            loadCurrentUser()

            rentalRepository.getRentalById(rentalId)
                .onSuccess { rental ->
                    _uiState.update { it.copy(rental = rental) }
                    fetchProperty(rental.propertyId)
                    fetchInventories(rental)
                    fetchTenant(rental.tenantId)
                    fetchReview(rental)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorRes = R.string.rental_details_error_loading)
                    }
                }
        }
    }

    fun deleteReview(reviewType: ReviewType) {
        viewModelScope.launch {
            val reviewId = uiState.value.review?.id ?: return@launch
            
            _uiState.update { it.copy(isDeletingReview = true) }
            
            deleteReviewUseCase(reviewId, reviewType)
                .onSuccess {
                    // Reload review to reflect changes
                    uiState.value.rental?.let { rental -> fetchReview(rental) }
                    _uiState.update { it.copy(isDeletingReview = false, deletionErrorMessageRes = R.string.review_delete_success) }
                }
                .onFailure {
                    _uiState.update { 
                        it.copy(isDeletingReview = false, deletionErrorMessageRes = R.string.review_delete_error)
                    }
                }
        }
    }

    fun clearDeletionErrorMessage() {
        _uiState.update { it.copy(deletionErrorMessageRes = null) }
    }

    private suspend fun fetchProperty(propertyId: String) {
        propertyRepository.getPropertyById(propertyId)
            .onSuccess { property ->
                _uiState.update {
                    it.copy(
                        property = property,
                        isLoading = false,
                        errorRes = null
                    )
                }
                if (property.landlordId.isNotBlank()) {
                    fetchLandlord(property.landlordId)
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(isLoading = false, errorRes = R.string.rental_details_error_loading)
                }
        }
    }

    private suspend fun fetchInventories(rental: Rental) {
        rental.entryInventoryId?.let { entryId ->
            inventoryRepository.getInventoryById(entryId)
                .onSuccess { inventory ->
                    _uiState.update { it.copy(entryInventory = inventory) }
                }
        }

        rental.exitInventoryId?.let { exitId ->
            inventoryRepository.getInventoryById(exitId)
                .onSuccess { inventory ->
                    _uiState.update { it.copy(exitInventory = inventory) }
                }
        }
    }

    private suspend fun fetchTenant(tenantId: String) {
        userRepository.getUserById(tenantId)
            .onSuccess { tenant ->
                _uiState.update { it.copy(tenant = tenant) }
            }
    }

    private suspend fun fetchReview(rental: Rental) {
        reviewRepository.getReviewByRental(rental.id)
            .onSuccess { review ->
                // review can be null if not found, which is valid (no review yet)
                _uiState.update { it.copy(review = review) }
            }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.update { it.copy(currentUser = user) }
                }
        }
    }

    private suspend fun fetchLandlord(landlordId: String) {
        userRepository.getUserById(landlordId)
            .onSuccess { landlord ->
                _uiState.update { it.copy(landlord = landlord) }
            }
    }
}
