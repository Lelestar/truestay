package ca.uqac.inf865.truestay.presentation.shared.inventory

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import ca.uqac.inf865.truestay.domain.usecase.inventory.SignInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignatureUiState(
    val inventory: Inventory? = null,
    val property: Property? = null,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitError: String? = null
)

@HiltViewModel
class SignatureViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val rentalRepository: RentalRepository,
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val signInventoryUseCase: SignInventoryUseCase,
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val inventoryId: String = savedStateHandle.get<String>("inventoryId") ?: ""

    var uiState by mutableStateOf(SignatureUiState(isLoading = true))
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, submitError = null)
            try {
                // 1. Load Current Firebase user then fetch profile
                val authUser = authRepository.getCurrentUser().getOrNull()
                if (authUser == null) {
                    uiState = uiState.copy(isLoading = false, error = context.getString(R.string.signature_load_error))
                    return@launch
                }
                val currentUser = userRepository.getUserById(authUser.id).getOrThrow()

                // 2. Load Inventory
                val inventoryResult = inventoryRepository.getInventoryById(inventoryId)
                val inventory = inventoryResult.getOrThrow()

                // 3. Load Property (via rental to get property id)
                val rental = rentalRepository.getRentalById(inventory.rentalId).getOrThrow()
                val propertyResult = propertyRepository.getPropertyById(rental.propertyId)
                val property = propertyResult.getOrThrow()

                uiState = uiState.copy(
                    inventory = inventory,
                    property = property,
                    currentUser = currentUser,
                    isLoading = false,
                    error = null,
                    submitError = null
                )

            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = context.getString(R.string.signature_load_error))
            }
        }
    }

    fun submitSignature(signatureBitmap: Bitmap, onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, submitError = null)
            try {
                val currentUser = uiState.currentUser ?: throw IllegalStateException(context.getString(R.string.signature_load_error)) // Generic load error for "not authenticated"
                signInventoryUseCase(inventoryId, currentUser.id, signatureBitmap).getOrThrow()
                uiState = uiState.copy(isSubmitting = false, submitError = null)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(isSubmitting = false, submitError = context.getString(R.string.signature_submit_error))
            }
        }
    }
}
