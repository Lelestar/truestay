package ca.uqac.inf865.truestay.presentation.tenant.rentals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentalsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    var uiState by mutableStateOf(RentalsUiState())
        private set

    fun loadRentals(tenantId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            rentalRepository.getRentalsByTenant(tenantId)
                .onSuccess { rentals ->
                    // Séparer la location active des locations historiques
                    val activeRental = rentals.firstOrNull { it.status == RentalStatus.ACTIVE }
                    val historicalRentals = rentals.filter {
                        it.status == RentalStatus.ENDED || it.status == RentalStatus.CANCELLED
                    }

                    // Charger les propriétés associées
                    loadPropertiesForRentals(rentals, activeRental, historicalRentals)
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Une erreur est survenue"
                    )
                }
        }
    }

    private suspend fun loadPropertiesForRentals(
        allRentals: List<Rental>,
        activeRental: Rental?,
        historicalRentals: List<Rental>
    ) {
        // Charger toutes les propriétés en parallèle
        val propertyIds = allRentals.map { it.propertyId }.distinct()
        val propertiesMap = mutableMapOf<String, Property>()

        propertyIds.forEach { propertyId ->
            propertyRepository.getPropertyById(propertyId)
                .onSuccess { property ->
                    propertiesMap[propertyId] = property
                }
        }

        // Créer les RentalWithProperty
        val activeRentalWithProperty = activeRental?.let { rental ->
            propertiesMap[rental.propertyId]?.let { property ->
                RentalWithProperty(rental, property)
            }
        }

        val historicalRentalsWithProperty = historicalRentals.mapNotNull { rental ->
            propertiesMap[rental.propertyId]?.let { property ->
                RentalWithProperty(rental, property)
            }
        }

        uiState = uiState.copy(
            isLoading = false,
            activeRental = activeRentalWithProperty,
            historicalRentals = historicalRentalsWithProperty
        )
    }
}

data class RentalsUiState(
    val isLoading: Boolean = false,
    val activeRental: RentalWithProperty? = null,
    val historicalRentals: List<RentalWithProperty> = emptyList(),
    val error: String? = null
)

data class RentalWithProperty(
    val rental: Rental,
    val property: Property
)
