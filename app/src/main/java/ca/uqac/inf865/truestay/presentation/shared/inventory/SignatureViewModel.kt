package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.usecase.inventory.SignInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignatureViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val signInventoryUseCase: SignInventoryUseCase
) : ViewModel() {
    // TODO: Implement signature logic
}