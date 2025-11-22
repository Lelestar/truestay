package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import ca.uqac.inf865.truestay.domain.usecase.inventory.GenerateInventoryPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InventoryError {
    LOAD_FAILED
}

data class InventoryUiState(
    val inventory: Inventory? = null,
    val propertyName: String = "",
    val propertyAddress: String = "",
    val landlordName: String = "",
    val tenantName: String = "",
    val currentUserId: String = "",
    val landlordId: String = "",
    val tenantId: String = "",
    val isLoading: Boolean = false,
    val error: InventoryError? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val rentalRepository: RentalRepository,
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository,
    private val authRepository: ca.uqac.inf865.truestay.domain.repository.AuthRepository,
    private val generateInventoryPdfUseCase: GenerateInventoryPdfUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryUiState(isLoading = true))
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    fun loadInventory(inventoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            inventoryRepository.getInventoryById(inventoryId)
                .onSuccess { inventory ->
                    fetchPropertyDetails(inventory)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = InventoryError.LOAD_FAILED
                        )
                    }
                }
        }
    }

    private suspend fun fetchPropertyDetails(inventory: Inventory) {
        val currentUser = authRepository.getCurrentUser().getOrNull()
        val currentUserId = currentUser?.id.orEmpty()

        rentalRepository.getRentalById(inventory.rentalId)
            .onSuccess { rental ->
                propertyRepository.getPropertyById(rental.propertyId)
                    .onSuccess { property ->
                        val landlordName = userRepository.getUserById(rental.landlordId)
                            .getOrNull()
                            ?.let { user -> "${user.firstName} ${user.lastName}" }
                            .orEmpty()
                        val tenantName = userRepository.getUserById(rental.tenantId)
                            .getOrNull()
                            ?.let { user -> "${user.firstName} ${user.lastName}" }
                            .orEmpty()

                        _uiState.update {
                            it.copy(
                                inventory = inventory,
                                propertyName = property.name,
                                propertyAddress = "${property.address.street}, ${property.address.city}",
                                landlordName = landlordName,
                                tenantName = tenantName,
                                currentUserId = currentUserId,
                                landlordId = rental.landlordId,
                                tenantId = rental.tenantId,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure { setLoadError() }
            }
            .onFailure { setLoadError() }
    }

    private fun setLoadError() {
        _uiState.update {
            it.copy(
                inventory = null,
                propertyName = "",
                propertyAddress = "",
                isLoading = false,
                error = InventoryError.LOAD_FAILED
            )
        }
    }
}
