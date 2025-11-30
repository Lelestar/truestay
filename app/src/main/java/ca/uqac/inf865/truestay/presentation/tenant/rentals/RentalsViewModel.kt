package ca.uqac.inf865.truestay.presentation.tenant.rentals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Combination of a rental and its associated property for display
 */
data class RentalPropertyItem(
    val rental: Rental,
    val property: Property,
    val tenantRating: Float? = null
)

/**
 * Types of errors that may occur in the rentals screen
 */
enum class RentalsError {
    NOT_AUTHENTICATED,
    LOAD_FAILED
}

/**
 * UI state for the rentals screen
 */
data class RentalsUiState(
    val currentRental: RentalPropertyItem? = null,
    val pendingRentals : List<RentalPropertyItem> = emptyList(),
    val pastRentals: List<RentalPropertyItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: RentalsError? = null,
    val snackbarMessage: String? = null
)

/**
 * ViewModel for managing the tenant's rentals screen
 *
 * Manages the loading and display of current and past rentals.
 * Separates active rentals from historical ones for better organization.
 */
@HiltViewModel
class RentalsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalsUiState(isLoading = true))
    val uiState: StateFlow<RentalsUiState> = _uiState.asStateFlow()

    init {
        observeRentals()
    }

    /**
     * Observes rentals in real-time for the current user
     */
    private fun observeRentals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentRental = null,
                                pendingRentals = emptyList(),
                                pastRentals = emptyList(),
                                error = RentalsError.NOT_AUTHENTICATED
                            )
                        }
                        return@onSuccess
                    }

                    // Observe rentals in real-time
                    rentalRepository.observeRentalsByTenant(user.id)
                        .collect { rentals ->
                            processRentals(rentals)
                        }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentRental = null,
                            pendingRentals = emptyList(),
                            pastRentals = emptyList(),
                            error = RentalsError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    /**
     * Reloads the rentals list from the repositories
     */
    fun refreshRentals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentRental = null,
                                pastRentals = emptyList(),
                                error = RentalsError.NOT_AUTHENTICATED
                            )
                        }
                        return@onSuccess
                    }
                    fetchRentalsForUser(user.id)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentRental = null,
                            pastRentals = emptyList(),
                            error = RentalsError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    /**
     * Processes rentals and combines them with property information
     */
    private suspend fun processRentals(rentals: List<Rental>) {
        if (rentals.isEmpty()) {
            _uiState.update {
                it.copy(
                    currentRental = null,
                    pendingRentals = emptyList(),
                    pastRentals = emptyList(),
                    isLoading = false,
                    error = null
                )
            }
            return
        }

        propertyRepository.getProperties()
            .onSuccess { properties ->
                val propertyMap = properties.associateBy { it.id }
                val baseItems = rentals.mapNotNull { rental ->
                    propertyMap[rental.propertyId]?.let { property ->
                        RentalPropertyItem(rental = rental, property = property)
                    }
                }

                // Enrich with tenant's rating when available
                val items = baseItems.map { item ->
                    val rating = reviewRepository.getReviewByRental(item.rental.id)
                        .getOrNull()
                        ?.propertyReview
                        ?.overallRating
                        ?.takeIf { it > 0f }

                    if (rating != null) {
                        item.copy(tenantRating = rating)
                    } else {
                        item
                    }
                }
                val pending = items.filter { it.rental.status == RentalStatus.PENDING }

                // Separate current (ACTIVE) rental from past rentals
                val current = items.firstOrNull { it.rental.status == RentalStatus.ACTIVE }
                val past = items
                    .filter { it.rental.status == RentalStatus.ENDED }
                    .sortedByDescending { it.rental.endDate } // Most recent first

                _uiState.update {
                    it.copy(
                        currentRental = current,
                        pendingRentals = pending,
                        pastRentals = past,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        currentRental = null,
                        pendingRentals = emptyList(),
                        pastRentals = emptyList(),
                        isLoading = false,
                        error = RentalsError.LOAD_FAILED
                    )
                }
            }
    }

    /**
     * Retrieves a user's rentals and combines them with property information
     */
    private suspend fun fetchRentalsForUser(userId: String) {
        rentalRepository.getRentalsByTenant(userId)
            .onSuccess { rentals ->
                processRentals(rentals)
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        currentRental = null,
                        pendingRentals = emptyList(),
                        pastRentals = emptyList(),
                        isLoading = false,
                        error = RentalsError.LOAD_FAILED
                    )
                }
            }
    }


    fun acceptRental(rentalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = null) }

            rentalRepository.getRentalById(rentalId)
                .onSuccess { rental ->
                    val updatedRental = rental.copy(
                        status = RentalStatus.ACTIVE,
                        acceptedAt = System.currentTimeMillis()
                    )
                    rentalRepository.updateRental(updatedRental)
                        .onSuccess {
                            _uiState.update {
                                it.copy(snackbarMessage = "RENTAL_REQUEST_ACCEPTED")
                            }
                            refreshRentals()
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(snackbarMessage = "RENTAL_REQUEST_ACCEPT_ERROR|${error.message}")
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(snackbarMessage = "RENTAL_REQUEST_LOAD_ERROR|${error.message}")
                    }
                }
        }
    }

    fun declineRental(rentalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = null) }

            rentalRepository.deleteRental(rentalId)
                .onSuccess {
                    _uiState.update {
                        it.copy(snackbarMessage = "RENTAL_REQUEST_DECLINED")
                    }
                    refreshRentals()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(snackbarMessage = "RENTAL_REQUEST_DECLINE_ERROR|${error.message}")
                    }
                }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
