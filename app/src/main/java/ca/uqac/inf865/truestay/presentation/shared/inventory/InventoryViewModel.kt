package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import ca.uqac.inf865.truestay.domain.usecase.inventory.GenerateInventoryPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val userRepository: UserRepository,
    private val generateInventoryPdfUseCase: GenerateInventoryPdfUseCase
) : ViewModel() {
    // TODO: Implement inventory logic
}