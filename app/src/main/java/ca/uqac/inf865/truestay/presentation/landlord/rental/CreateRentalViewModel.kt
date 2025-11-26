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


data class CreateRentalUiState(
    val property: Property? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CreateRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    // TODO: Implement create rental logic
    var uiState = mutableStateOf(CreateRentalUiState())
        private set
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
//                    loadReviewIfExists(property.id)
//                    if (property.landlordId.isNotBlank()) {
//                        loadLandlord(property.landlordId)
//                    }
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }

        }
    }
}

