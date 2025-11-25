package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a rental with associated property and tenant information
 */
data class LandlordRentalItem(
    val rental: Rental,
    val property: Property,
    val tenant: User
)

/**
 * Types of errors that may occur in the landlord rentals screen
 */
enum class LandlordRentalsError {
    NOT_AUTHENTICATED,
    LOAD_FAILED
}

/**
 * UI state for the landlord rentals screen
 */
data class LandlordRentalsUiState(
    val activeRentals: List<LandlordRentalItem> = emptyList(),
    val pastRentals: List<LandlordRentalItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: LandlordRentalsError? = null
)

/**
 * ViewModel for managing the landlord's rentals screen
 *
 * Manages the loading and display of active and past rentals for properties owned by the landlord.
 * Separates active rentals (ACTIVE status) from past rentals (ENDED or CANCELLED status).
 */
@HiltViewModel
class RentalsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandlordRentalsUiState(isLoading = true))
    val uiState: StateFlow<LandlordRentalsUiState> = _uiState.asStateFlow()

    init {
        refreshRentals()
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
                                activeRentals = emptyList(),
                                pastRentals = emptyList(),
                                error = LandlordRentalsError.NOT_AUTHENTICATED
                            )
                        }
                        return@onSuccess
                    }
                    fetchRentalsForLandlord(user.id)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeRentals = emptyList(),
                            pastRentals = emptyList(),
                            error = LandlordRentalsError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    /**
     * Retrieves a landlord's rentals and combines them with property and tenant information
     */
    private suspend fun fetchRentalsForLandlord(landlordId: String) {
        rentalRepository.getRentalsByLandlord(landlordId)
            .onSuccess { rentals ->
                if (rentals.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            activeRentals = emptyList(),
                            pastRentals = emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    return@onSuccess
                }

                // Fetch properties
                propertyRepository.getProperties()
                    .onSuccess { properties ->
                        val propertyMap = properties.associateBy { it.id }

                        // Fetch all tenant information
                        val tenantIds = rentals.map { it.tenantId }.distinct()
                        val tenantMap = mutableMapOf<String, User>()

                        tenantIds.forEach { tenantId ->
                            userRepository.getUserById(tenantId)
                                .onSuccess { tenant ->
                                    if (tenant != null) {
                                        tenantMap[tenantId] = tenant
                                    }
                                }
                        }

                        // Combine rental data
                        val items = rentals.mapNotNull { rental ->
                            val property = propertyMap[rental.propertyId]
                            val tenant = tenantMap[rental.tenantId]
                            if (property != null && tenant != null) {
                                LandlordRentalItem(
                                    rental = rental,
                                    property = property,
                                    tenant = tenant
                                )
                            } else {
                                null
                            }
                        }

                        // Separate active from past rentals
                        val active = items
                            .filter { it.rental.status == RentalStatus.ACTIVE }
                            .sortedByDescending { it.rental.startDate }

                        val past = items
                            .filter { it.rental.status == RentalStatus.ENDED || it.rental.status == RentalStatus.CANCELLED }
                            .sortedByDescending { it.rental.endDate }

                        _uiState.update {
                            it.copy(
                                activeRentals = active,
                                pastRentals = past,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(
                                activeRentals = emptyList(),
                                pastRentals = emptyList(),
                                isLoading = false,
                                error = LandlordRentalsError.LOAD_FAILED
                            )
                        }
                    }
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        activeRentals = emptyList(),
                        pastRentals = emptyList(),
                        isLoading = false,
                        error = LandlordRentalsError.LOAD_FAILED
                    )
                }
            }
    }
}