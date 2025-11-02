package ca.uqac.inf865.truestay.presentation.shared.rental

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RentalDetailsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val inventoryRepository: InventoryRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // TODO: Implement rental details logic
}