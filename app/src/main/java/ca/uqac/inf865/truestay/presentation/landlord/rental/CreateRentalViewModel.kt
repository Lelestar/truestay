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
import java.util.Locale


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

    fun createRental(
        propertyId: String,
        tenantEmail: String,
        startDate: String,
        endDate: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            // 1) récupérer le locataire via email
            val tenant = userRepository.getUserByEmail(tenantEmail).getOrNull()
            println("TENANT = $tenant")

            if(tenant == null) {
                onError("Aucun utilisateur trouvé avec cet email")
                return@launch
            }

            // 2) récupérer le propriétaire connecté
            val currentUser = authRepository.getCurrentUser().getOrNull()

            val landlordId = currentUser?.id
            if(landlordId == null) {
                onError("Impossinle d'identifier le propriétaire")
                uiState.value = uiState.value.copy(isLoading = false)
                return@launch
            }
            println("USER = $landlordId")

            // 3️⃣ convertir les dates (dd/MM/yyyy → timestamp)
            val startMillis = parseDate(startDate)
            val endMillis = parseDate(endDate)

            // 4️⃣ construire la location
            val rental = Rental(
                id = "",
                propertyId = propertyId,
                tenantId = tenant.id,
                landlordId = landlordId,
                startDate = startMillis,
                endDate = endMillis
            )

            // 5️⃣ envoyer au repository
            rentalRepository.createRental(rental)
                .onSuccess { docId ->

                    rentalRepository.updateRentalId(docId)
                    rentalRepository.updateRental(rental.copy(id = docId))

                    uiState.value = uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure {
                    uiState.value = uiState.value.copy(isLoading = false)
                    onError(it.message ?: "Erreur lors de la création")
                }
        }
    }

    private fun parseDate(date: String): Long {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.parse(date)?.time ?: 0L
    }


}

