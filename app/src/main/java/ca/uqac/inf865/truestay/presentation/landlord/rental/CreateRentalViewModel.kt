package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.presentation.shared.property.ReviewFilterType
import ca.uqac.inf865.truestay.presentation.shared.property.ReviewWithUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class CreateRentalUiState(
    val property: Property? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val tenantEmail: String = "",
    val startDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
    val endDate: String = "",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class CreateRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState = mutableStateOf(CreateRentalUiState())
        private set

    fun updateTenantEmail(email: String) {
        uiState.value = uiState.value.copy(tenantEmail = email)
    }

    fun updateStartDate(date: String) {
        uiState.value = uiState.value.copy(startDate = date)
    }

    fun updateEndDate(date: String) {
        uiState.value = uiState.value.copy(endDate = date)
    }

    fun dismissError() {
        uiState.value = uiState.value.copy(errorMessage = null)
    }

    fun loadProperty(propertyId: String) {
        viewModelScope.launch {
            // Load property
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            propertyRepository.getPropertyById(propertyId)
                .onSuccess { property ->
                    uiState.value = uiState.value.copy(
                        property = property,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }

        }
    }

    /**
     * Create a new rental with tenant email and date range
     */
    fun createRental(
        propertyId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isSubmitting = true, errorMessage = null)

            // 1) Get tenant by email
            val tenant = userRepository.getUserByEmail(uiState.value.tenantEmail).getOrNull()

            if (tenant == null) {
                uiState.value = uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "TENANT_NOT_FOUND"
                )
                return@launch
            }

            // 2) Check if tenant already has an active or pending rental
            val existingRentals = rentalRepository.getRentalsByTenant(tenant.id).getOrNull()
            val hasActiveRental = existingRentals?.any {
                it.status == ca.uqac.inf865.truestay.domain.model.RentalStatus.ACTIVE ||
                it.status == ca.uqac.inf865.truestay.domain.model.RentalStatus.PENDING
            } ?: false

            if (hasActiveRental) {
                uiState.value = uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "TENANT_ALREADY_HAS_ACTIVE_RENTAL"
                )
                return@launch
            }

            // 3) Get current landlord
            val currentUser = authRepository.getCurrentUser().getOrNull()
            val landlordId = currentUser?.id

            if (landlordId == null) {
                uiState.value = uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "LANDLORD_NOT_IDENTIFIED"
                )
                return@launch
            }

            // 4) Convert dates (dd/MM/yyyy → timestamp)
            val startMillis = parseDate(uiState.value.startDate)
            val endMillis = parseDate(uiState.value.endDate)

            // 5) Build rental object
            val rental = Rental(
                id = "",
                propertyId = propertyId,
                tenantId = tenant.id,
                landlordId = landlordId,
                startDate = startMillis,
                endDate = endMillis,
                createdAt = System.currentTimeMillis()
            )

            // 6) Save to repository
            rentalRepository.createRental(rental)
                .onSuccess { docId ->
                    // Update the rental with its generated ID
                    rentalRepository.updateRental(rental.copy(id = docId))
                        .onSuccess {
                            uiState.value = uiState.value.copy(isSubmitting = false)
                            onSuccess()
                        }
                        .onFailure { error ->
                            uiState.value = uiState.value.copy(
                                isSubmitting = false,
                                errorMessage = "CREATE_RENTAL_ERROR|${error.message}"
                            )
                        }
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "CREATE_RENTAL_ERROR|${error.message ?: "Unknown error"}"
                    )
                }
        }
    }

    /**
     * Parse date string (dd/MM/yyyy) to timestamp
     */
    private fun parseDate(date: String): Long {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.parse(date)?.time ?: 0L
    }
}

