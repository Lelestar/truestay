package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    // TODO: Implement room details logic
}